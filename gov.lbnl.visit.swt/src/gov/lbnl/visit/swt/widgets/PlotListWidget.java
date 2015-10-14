package gov.lbnl.visit.swt.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.AttributeSubject;
import visit.java.client.FileInfo;
import visit.java.client.components.DatabaseInformation;
import visit.java.client.components.DatabaseInformation.DatabaseInformationUpdateCallback;
import visit.java.client.components.PlotList;
import visit.java.client.components.PlotList.Plot;
import visit.java.client.components.PlotList.PlotListCallback;

public class PlotListWidget extends VisItWidget {
	public interface PlotListWidgetCallback {
		void drawPlot(String plot, String variable);
		void setColormap(String plotType, String colormap);
	}
	
	Tree vars;
	Menu varsMenu;

	Tree tree;
	Menu plotsMenu;
	PlotList plotlist;
	
	DatabaseInformation databaseInfo;
	
	Composite editorComposite;
	ArrayList<TreeItem> changes;
	
	ArrayList<PlotListWidgetCallback> cbList = new ArrayList<PlotListWidgetCallback>();
	
	public PlotListWidget(Composite parent, int style) {
		super(parent, style, null);
		setupUI();
	}
	
	public PlotListWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style, null);
		setVisItSwtConnection(conn);
		setupUI();
	}
	
	public void setVisItSwtConnection(VisItSwtConnection conn) {
		connection = conn;
		plotlist = new PlotList(conn.getViewerMethods());
		databaseInfo = new DatabaseInformation(connection.getViewerMethods());

		final PlotListCallback pcb = new PlotListCallback() {
			
			@Override
			public void plotList(ArrayList<Plot> p) {
				
				final ArrayList<Plot> plots = p;
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						tree.removeAll();
						
						for(int i = 0; i < plots.size(); ++i) {
							TreeItem item = new TreeItem(tree, SWT.NONE);
							item.setText(new String [] { plots.get(i).plotTypeName , 
								    "(" + plots.get(i).var + ")" });
							
							item.setExpanded(true);
							
							TreeItem opt = new TreeItem(item, SWT.NONE);
							opt.setText(new String[] { "options", "-" } );
							
							///
							String atsubj = plots.get(i).plotTypeName + "Attributes";
							AttributeSubject subj = connection.getViewerState().getAttributeSubjectFromTypename(atsubj);
							JsonObject obj = subj.getApi();
							
							Iterator< Entry<String,JsonElement> > itr = obj.entrySet().iterator();
							
							while(itr.hasNext()) {
								Entry<String, JsonElement> e = itr.next();
								TreeItem child = new TreeItem(opt, SWT.NONE);
								try {
									child.setText(new String[] {e.getKey(), subj.get(e.getKey()).getAsString()});
								} catch(Exception ex) {
									child.setText(new String[] {e.getKey(), "N/A"});
								}
								child.setExpanded(true);
							}
							
							for(int j = 0; j < plots.get(i).ops.size(); ++j) {
								TreeItem opitem = new TreeItem(item, SWT.NONE);
								opitem.setText(new String[] { plots.get(i).ops.get(j).name, "(operator)"});
								opitem.setExpanded(true);
									
								String opname = plots.get(i).ops.get(j).name + "Attributes";
								AttributeSubject opsubj = connection.getViewerState().getAttributeSubjectFromTypename(opname);
								JsonObject opobj = opsubj.getApi();
								
								Iterator< Entry<String,JsonElement> > opitr = opobj.entrySet().iterator();
								while(opitr.hasNext()) {
									Entry<String, JsonElement> e = opitr.next();
									
									TreeItem child = new TreeItem(opitem, SWT.NONE);
									
									try {
										child.setText(new String[] {e.getKey(), opsubj.get(e.getKey()).getAsString()});
									} catch(Exception ex) {
										child.setText(new String[] {e.getKey(), "N/A"});
									}
									child.setExpanded(true);
								}
							}
						}
					}
				});
			}
		};
		
		plotlist.register(pcb);

		final DatabaseInformationUpdateCallback dib = new DatabaseInformationUpdateCallback() {
			
			@Override
			public void vars(FileInfo info) {
				final FileInfo fi = info;
				getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						load(fi);
					}
				});
			}
		};
		
		databaseInfo.register(dib);
		
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				//System.out.println("Plot List Disposing...");
				plotlist.unregister(pcb);
				databaseInfo.unregister(dib);
				
				plotlist.cleanup();
				databaseInfo.cleanup();
			}
		});
	}
    
	public boolean register(PlotListWidgetCallback cb) {
		return cbList.add(cb);
	}
	
	public boolean unregister(PlotListWidgetCallback cb) {
		return cbList.remove(cb);
	}
	
	public void load(FileInfo info) {
		  vars.removeAll();

		  if(info.getScalars().size() > 0) {
			  TreeItem scalars = new TreeItem(vars, SWT.NONE);
			  scalars.setText("Scalars");
			  for(int i = 0; i < info.getScalars().size(); ++i) {
				  new TreeItem(scalars, SWT.NONE).setText(info.getScalars().get(i));
			  }
			  scalars.setExpanded(true);
		  }
		  
		  if(info.getMeshes().size() > 0 ) {
			  TreeItem meshes = new TreeItem(vars, SWT.NONE);
			  meshes.setText("Meshes");
			  for(int i = 0; i < info.getMeshes().size(); ++i) {
				  new TreeItem(meshes, SWT.NONE).setText(info.getMeshes().get(i));
			  }
			  meshes.setExpanded(true);
		  }
		  
		  if(info.getVectors().size() > 0) {
			  TreeItem vectors = new TreeItem(vars, SWT.NONE);
			  vectors.setText("Vectors");
			  for(int i = 0; i < info.getVectors().size(); ++i) {
				  new TreeItem(vectors, SWT.NONE).setText(info.getVectors().get(i));
			  }
			  vectors.setExpanded(true);
		  }

		  if(info.getMaterials().size() > 0) {
			  TreeItem materials = new TreeItem(vars, SWT.NONE);
			  materials.setText("Materials");
			  for(int i = 0; i < info.getMaterials().size(); ++i) {
				  new TreeItem(materials, SWT.NONE).setText(info.getMaterials().get(i));
			  }
			  materials.setExpanded(true);
		  }
	  }
	
	private void setupUI() {
		Composite comp = new Composite(this, SWT.NONE);
		//comp.setLayout(new FillLayout(SWT.VERTICAL));
		comp.setLayout(new GridLayout(1, true));
		
		vars = new Tree(comp, SWT.VIRTUAL | SWT.BORDER | 
				SWT.H_SCROLL | SWT.V_SCROLL);
		vars.setHeaderVisible(true);
		varsMenu = new Menu(vars);
		vars.setMenu(varsMenu);
		
	    varsMenu.addMenuListener(new MenuAdapter()
	    {
	        public void menuShown(MenuEvent e)
	        {
	            MenuItem[] items = varsMenu.getItems();
	            for (int i = 0; i < items.length; i++)
	            {
	                items[i].dispose();
	            }
	            
	            if(vars.getSelectionCount() == 0)
	            	return;
	            
	            TreeItem item = vars.getSelection()[0];
	            
	            ///if parent is null
	            if(item.getParentItem() == null) {
	            	return;
	            }
	            
	            //String text = item.getParentItem().getText();
	            
	            List<String> plotList = connection.getViewerMethods().getPlotList();
	            for(int i = 0; i < plotList.size(); ++i) {
	            	MenuItem m = new MenuItem(varsMenu, SWT.NONE);
	            	m.setText(plotList.get(i));
	            	
	            	m.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							MenuItem item = (MenuItem)e.widget;
							String variable = vars.getSelection()[0].getText();
						    String plot = item.getText();
						    for(int i = 0; i < cbList.size(); ++i)
						    	cbList.get(i).drawPlot(plot, variable);
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
	            }
	        }
	    });
	    
		tree = new Tree(comp, SWT.VIRTUAL | SWT.BORDER | 
									SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(true);
		
		TreeColumn column1 = new TreeColumn(tree, SWT.CENTER);
		column1.setText("Key");
		column1.setWidth(100);
		column1.setResizable(true);

		TreeColumn column2 = new TreeColumn(tree, SWT.CENTER);
		column2.setText("Value");
		column2.setWidth(100);
		column2.setResizable(true);
		
		editorComposite = new Composite(comp, SWT.BORDER);
		editorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Button updateOps = new Button(comp, SWT.NONE);
		updateOps.setText("Commit Changes...");
		updateOps.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		updateOps.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		plotsMenu = new Menu(tree);
		
		MenuItem deleteItem = new MenuItem(plotsMenu, SWT.NONE);
		deleteItem.setText("Delete");
		
		deleteItem.addSelectionListener(new SelectionListener() {		
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = tree.getSelection()[0];
				while(item.getParentItem() != null) {
					item = item.getParentItem();
				}
				
				int index = -1;
				TreeItem items[] = tree.getItems();
				for(int i = 0; i < items.length; ++i) {
					if(items[i] == item) {
						index = i;
						break;
					}
				}
				if(index == -1) return;
				
				connection.getViewerMethods().setActivePlots(index);
				connection.getViewerMethods().deleteActivePlots();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		MenuItem colorItem = new MenuItem(plotsMenu, SWT.CASCADE);
		colorItem.setText("Colors...");
		
		final Menu colorMenu = new Menu(colorItem);
		colorItem.setMenu(colorMenu);
		
//		MenuItem changeVarItem = new MenuItem(plotsMenu, SWT.CASCADE);
//		changeVarItem.setText("Change Var...");

//		final Menu changeVarMenu = new Menu(changeVarItem);
//		changeVarItem.setMenu(changeVarMenu);
		
		MenuItem operatorItem = new MenuItem(plotsMenu, SWT.CASCADE);
		operatorItem.setText("Operator...");
		
		final Menu operatorVarMenu = new Menu(operatorItem);
		operatorItem.setMenu(operatorVarMenu);
		
		tree.setMenu(plotsMenu);
		
//		final TreeItem [] lastItem = new TreeItem [1];
//		final TreeItem editor = new TreeEditor (tree);
//		final Color black = getDisplay().getSystemColor (SWT.COLOR_BLACK);
		tree.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem item = tree.getItem(new Point(e.x, e.y));
				editValue(item);
			}
		});
		
		colorMenu.addMenuListener(new MenuAdapter()
	    {
	        public void menuShown(MenuEvent e)
	        {
	            MenuItem[] items = colorMenu.getItems();
	            for (int i = 0; i < items.length; i++) {
	                items[i].dispose();
	            }
	            
	            if(tree.getSelectionCount() == 0) return;
	            
	            AttributeSubject catts = connection.getViewerState().getAttributeSubjectFromTypename("ColorTableAttributes");
	            List<String> colornames = catts.getAsStringVector("names");
	            for(int i = 0; i < colornames.size(); ++i) {
	            	MenuItem m = new MenuItem(colorMenu, SWT.NONE);
	            	m.setText(colornames.get(i));
	            	
	            	m.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							TreeItem item = tree.getSelection()[0];
							while(item.getParentItem() != null) {
								item = item.getParentItem();
							}
							
							MenuItem ex = (MenuItem)e.widget;
							String text = ex.getText();
							
							//System.out.println(item.getText() + " " + text);
							//setColormap(item.getText(), text);
							for(int i = 0; i < cbList.size(); ++i) {
								cbList.get(i).setColormap(item.getText(), text);
							}
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
	            }
	        }
	    });
		
//		changeVarMenu.addMenuListener(new MenuAdapter()
//	    {
//	        public void menuShown(MenuEvent e)
//	        {
//	            MenuItem[] items = changeVarMenu.getItems();
//	            for (int i = 0; i < items.length; i++) {
//	                items[i].dispose();
//	            }
//	            
//	            if(tree.getSelectionCount() == 0) return;
//	            
//	            ///TODO: change variable...
//	        }
//	    });
		
		operatorVarMenu.addMenuListener(new MenuAdapter()
	    {
	        public void menuShown(MenuEvent e)
	        {
	            MenuItem[] items = operatorVarMenu.getItems();
	            for (int i = 0; i < items.length; i++) {
	                items[i].dispose();
	            }
	            
	            if(tree.getSelectionCount() == 0) return;
	            
	            List<String> operatorList = connection.getViewerMethods().getOperatorList();
	            for(int i = 0; i < operatorList.size(); ++i) {
	            	MenuItem m = new MenuItem(operatorVarMenu, SWT.NONE);
	            	m.setText(operatorList.get(i));
	            	m.addSelectionListener(new SelectionListener() {
						
						@Override
						public void widgetSelected(SelectionEvent e) {
							TreeItem item = tree.getSelection()[0];
							while(item.getParentItem() != null) {
								item = item.getParentItem();
							}
							
							int index = -1;
							TreeItem items[] = tree.getItems();
							for(int i = 0; i < items.length; ++i) {
								if(items[i] == item) {
									index = i;
									break;
								}
							}
							if(index == -1) return;
							
							MenuItem mitem = (MenuItem) e.widget;
							
							connection.getViewerMethods().setActivePlots(index);
							connection.getViewerMethods().addOperator(mitem.getText());
							connection.getViewerMethods().drawPlots();
						}
						
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
	            }
	        }
	    });
	}

	public void editValue(TreeItem item) {
		
		//final TreeItem item = (TreeItem) e.item;

		if(item == null) return;
		if(item.getParentItem() == null) return;
		
		String key = item.getText(0);
		String value = item.getText(1);
		
		if(key.startsWith("options") || value.startsWith("(operator)")) {
			return;
		}

		TreeItem parentItem = item.getParentItem();
		
		int aPlot = -1, aOp = -1;
		String pPlot = "", pOp = "";

		if(parentItem.getText().startsWith("options")) { ///it is a plot (detect which one)
			parentItem = parentItem.getParentItem();
			pPlot = parentItem.getText();
			
			for(int i = 0; i < tree.getItemCount(); ++i) {
				if(parentItem == tree.getItem(i)) {
					aPlot = i;
					break;
				}
			}
		} else {
			/// it is an operator, compute active plot and operator...
			TreeItem parentPlotItem = parentItem.getParentItem();
			pOp = parentItem.getText();
			pPlot = parentPlotItem.getText();
			
			for(int i = 0; i < parentPlotItem.getItemCount(); ++i) {
				if(parentItem == parentPlotItem.getItem(i)) {
					aOp = i-1; //(because plot options take 0th spot
					break;
				}
			}
			
			for(int i = 0; i < tree.getItemCount(); ++i) {
				if(parentPlotItem == tree.getItem(i)) {
					aPlot = i;
					break;
				}
			}
		}

		///actual plot and operator..
		
	}
}


/*
final String parentPlot = pPlot;
final String parentOperator = pOp;

final int activePlot = aPlot;
final int activeOperator = aOp;

if (item != null && item == lastItem [0]) {
	
	final Composite composite = new Composite (tree, SWT.NONE);
	composite.setBackground (black);
	
	final Text text = new Text (composite, SWT.NONE);
	final int inset = 1;
	
	composite.addListener (SWT.Resize, new Listener () {
		@Override
		public void handleEvent (Event event) {
			Rectangle rect = composite.getClientArea ();
			text.setBounds (rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
		}
	});
	
	Listener textListener = new Listener () {
		@Override
		public void handleEvent (final Event e) {
			switch (e.type) {
				case SWT.FocusOut:
					item.setText(1, text.getText ());
					composite.dispose ();

					///TODO: update object <---
//					System.out.println("plot: " + parentPlot + " " + activePlot + " " 
//							+ parentOperator + " " + activeOperator);

					///editing plot
					if(parentOperator.length() == 0) {
						plotlist.updatePlot(parentPlot, activePlot, item.getText(0), item.getText(1));
					} else {
						/// editing operator..
						plotlist.updateOperator(parentPlot, activePlot, 
								parentOperator, activeOperator, item.getText(0), item.getText(1));
					}

					break;
				case SWT.Verify:
					String newText = text.getText ();
					String leftText = newText.substring (0, e.start);
					String rightText = newText.substring (e.end, newText.length ());
					GC gc = new GC (text);
					Point size = gc.textExtent (leftText + e.text + rightText);
					gc.dispose ();
					size = text.computeSize (size.x, SWT.DEFAULT);
					editor.horizontalAlignment = SWT.LEFT;
					Rectangle itemRect = item.getBounds (), rect = tree.getClientArea ();
					editor.minimumWidth = Math.max (size.x, itemRect.width) + inset * 2;
					int left = itemRect.x, right = rect.x + rect.width;
					editor.minimumWidth = Math.min (editor.minimumWidth, right - left);
					editor.minimumHeight = size.y + inset * 2;
					editor.layout ();
					break;
				case SWT.Traverse:
					switch (e.detail) {
						case SWT.TRAVERSE_RETURN:
							item.setText (1, text.getText ());
							//FALL THROUGH
						case SWT.TRAVERSE_ESCAPE:
							composite.dispose ();
							e.doit = false;
					}
					break;
			}
		}
	};
	text.addListener (SWT.FocusOut, textListener);
	text.addListener (SWT.Traverse, textListener);
	text.addListener (SWT.Verify, textListener);
	editor.setEditor (composite, item, 1);
	text.setText (value);
	text.selectAll ();
	text.setFocus ();
}
lastItem [0] = item;
*/
