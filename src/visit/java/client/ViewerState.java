package visit.java.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import visit.java.client.AttributeSubject.AttributeSubjectCallback;

/**
 * 
 * @authors hkq, tnp
 */
public class ViewerState {

	/**
	 * 
	 */
	public ArrayList<AttributeSubject> states;

	/**
	 * 
	 */
	private Hashtable<String, Integer> typenameToState;

	/**
	 * 
	 */
	private OutputStreamWriter output;

	/**
	 * 
	 */
	public ViewerState() {
		states = new ArrayList<AttributeSubject>();
		typenameToState = new Hashtable<String, Integer>();
	}

	/**
	 * @param jo
	 * @return
	 */
	public synchronized boolean update(JsonObject jo) {
		// if it does not have id, then class is not updated properly.
		if (!jo.has("id"))
			return false;

		JsonElement e = jo.get("id");
		int id = e.getAsInt();

		if (id < states.size())
			states.get(id).update(jo);
		else {
			int diff = id - states.size();

			states.add(new AttributeSubject());
			for (int i = 0; i < diff; ++i)
				states.add(new AttributeSubject());

			states.get(id).update(jo);
		}

		typenameToState.put(states.get(id).getTypename(), states.get(id)
				.getId());

		return true;
	}

	/**
	 * 
	 */
	public int getIndexFromTypename(String typename) {
		return typenameToState.get(typename);
	}
	
	public AttributeSubject getAttributeSubjectFromTypename(String typename) {
		
		int index = getIndexFromTypename(typename);
		
		if(index < 0)
			return null;
		
		return states.get(index);
	}

	/**
	 * @param index
	 * @return
	 */
	public synchronized AttributeSubject get(int index) {
		if (index < states.size())
			return states.get(index);
		return null;
	}

	/**
	 * 
	 * @param index
	 * @param key
	 * @return
	 */
	public synchronized JsonElement get(int index, String key) {
		if (index >= 0 && index < states.size())
			return states.get(index).get(key);
		return null;
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public JsonElement convertToJsonElement(Object o) {
		JsonElement e = null;

		if (o instanceof Boolean) {
			e = new JsonPrimitive((Boolean) o);
		} else if (o instanceof Number) {
			e = new JsonPrimitive((Number) o);
		} else if (o instanceof String) {
			e = new JsonPrimitive((String) o);
		} else if (o instanceof JsonElement) {
			e = ((JsonElement) o);
		} else if (o instanceof Collection) {
			e = convertToJsonElement(o);
		} else {
			e = new JsonPrimitive(o.toString());
		}

		return e;
	}

	/**
	 * 
	 * @param values
	 * @return
	 */
	public JsonElement convertToJsonElement(Collection<?> values) {
		JsonArray array = new JsonArray();

		Iterator<?> itr = values.iterator();
		while (itr.hasNext()) {
			JsonElement o = convertToJsonElement(itr.next());

			// Unconvertable to json..
			if (o == null) {
				return null;
			}

			array.add(o);
		}
		return array;
	}

	/**
	 * 
	 * @param index
	 * @param key
	 * @param value
	 */
	public synchronized void set(int index, String key, Boolean value) {
		set(index, key, new JsonPrimitive(value));
	}

	/**
	 * 
	 * @param index
	 * @param key
	 * @param value
	 */
	public synchronized void set(int index, String key, Number value) {
		set(index, key, new JsonPrimitive(value));
	}

	/**
	 * 
	 * @param index
	 * @param key
	 * @param value
	 */
	public synchronized void set(int index, String key, String value) {
		set(index, key, new JsonPrimitive(value));
	}

	/**
	 * 
	 * @param index
	 * @param key
	 * @param values
	 */
	public synchronized void set(int index, String key, Collection<?> values) {
		JsonElement e = convertToJsonElement(values);
		if (e == null) {
			System.err.println("Could not set values for key: " + key
					+ " index: " + index);
			return;
		}
		set(index, key, e);
	}

	/**
	 * 
	 * @param index
	 * @param key
	 * @param value
	 */
	public synchronized void set(int index, String key, JsonElement value) {
		if (index >= 0 && index < states.size())
			states.get(index).set(key, value);
	}

	/**
	 * 
	 * @param index
	 */
	public synchronized void notify(int index) {
		if (index >= 0 && index < states.size())
			states.get(index).notify(output);
	}

	/**
	 * 
	 * @param id
	 * @param callback
	 */
	public void registerCallback(String id, AttributeSubjectCallback callback) {
		for (int i = 0; i < states.size(); ++i) {
			String typename = states.get(i).getTypename();

			if (typename.equals(id)) {
				states.get(i).addCallback(callback);
			}
		}
	}

	/**
	 * @param o
	 */
	public synchronized void setConnection(OutputStreamWriter o) {
		output = o;
	}

	/**
	 * 
	 * @author hkq
	 */
	public enum RPCType {
        CloseRPC,
        DetachRPC,
        AddWindowRPC,
        DeleteWindowRPC,
        SetWindowLayoutRPC,
        SetActiveWindowRPC,
        ClearWindowRPC,
        ClearAllWindowsRPC,
        OpenDatabaseRPC,
        CloseDatabaseRPC,
        ActivateDatabaseRPC,
        CheckForNewStatesRPC,
        CreateDatabaseCorrelationRPC,
        AlterDatabaseCorrelationRPC,
        DeleteDatabaseCorrelationRPC,
        ReOpenDatabaseRPC,
        ReplaceDatabaseRPC,
        OverlayDatabaseRPC,
        OpenComputeEngineRPC,
        CloseComputeEngineRPC,
        AnimationSetNFramesRPC,
        AnimationPlayRPC,
        AnimationReversePlayRPC,
        AnimationStopRPC,
        TimeSliderNextStateRPC,
        TimeSliderPreviousStateRPC,
        SetTimeSliderStateRPC,
        SetActiveTimeSliderRPC,
        AddPlotRPC,
        AddEmbeddedPlotRPC,
        SetPlotFrameRangeRPC,
        DeletePlotKeyframeRPC,
        MovePlotKeyframeRPC,
        DeleteActivePlotsRPC,
        HideActivePlotsRPC,
        DrawPlotsRPC,
        DisableRedrawRPC,
        RedrawRPC,
        SetActivePlotsRPC,
        ChangeActivePlotsVarRPC,
        AddOperatorRPC,
        AddInitializedOperatorRPC,
        PromoteOperatorRPC,
        DemoteOperatorRPC,
        RemoveOperatorRPC,
        RemoveLastOperatorRPC,
        RemoveAllOperatorsRPC,
        SaveWindowRPC,
        SetDefaultPlotOptionsRPC,
        SetPlotOptionsRPC,
        SetDefaultOperatorOptionsRPC,
        SetOperatorOptionsRPC,
        WriteConfigFileRPC,
        ConnectToMetaDataServerRPC,
        IconifyAllWindowsRPC,
        DeIconifyAllWindowsRPC,
        ShowAllWindowsRPC,
        HideAllWindowsRPC,
        UpdateColorTableRPC,
        SetAnnotationAttributesRPC,
        SetDefaultAnnotationAttributesRPC,
        ResetAnnotationAttributesRPC,
        SetKeyframeAttributesRPC,
        SetPlotSILRestrictionRPC,
        SetViewAxisArrayRPC,
        SetViewCurveRPC,
        SetView2DRPC,
        SetView3DRPC,
        ResetPlotOptionsRPC,
        ResetOperatorOptionsRPC,
        SetAppearanceRPC,
        ProcessExpressionsRPC,
        SetLightListRPC,
        SetDefaultLightListRPC,
        ResetLightListRPC,
        SetAnimationAttributesRPC,
        SetWindowAreaRPC,
        PrintWindowRPC,
        ResetViewRPC,
        RecenterViewRPC,
        ToggleAllowPopupRPC,
        ToggleMaintainViewModeRPC,
        ToggleBoundingBoxModeRPC,
        ToggleCameraViewModeRPC,
        TogglePerspectiveViewRPC,
        ToggleSpinModeRPC,
        ToggleLockTimeRPC,
        ToggleLockToolsRPC,
        ToggleLockViewModeRPC,
        ToggleFullFrameRPC,
        UndoViewRPC,
        RedoViewRPC,
        InvertBackgroundRPC,
        ClearPickPointsRPC,
        SetWindowModeRPC,
        EnableToolRPC,
        SetToolUpdateModeRPC,
        CopyViewToWindowRPC,
        CopyLightingToWindowRPC,
        CopyAnnotationsToWindowRPC,
        CopyPlotsToWindowRPC,
        ClearCacheRPC,
        ClearCacheForAllEnginesRPC,
        SetViewExtentsTypeRPC,
        ClearRefLinesRPC,
        SetRenderingAttributesRPC,
        QueryRPC,
        CloneWindowRPC,
        SetMaterialAttributesRPC,
        SetDefaultMaterialAttributesRPC,
        ResetMaterialAttributesRPC,
        SetPlotDatabaseStateRPC,
        DeletePlotDatabaseKeyframeRPC,
        MovePlotDatabaseKeyframeRPC,
        ClearViewKeyframesRPC,
        DeleteViewKeyframeRPC,
        MoveViewKeyframeRPC,
        SetViewKeyframeRPC,
        OpenMDServerRPC,
        EnableToolbarRPC,
        HideToolbarsRPC,
        HideToolbarsForAllWindowsRPC,
        ShowToolbarsRPC,
        ShowToolbarsForAllWindowsRPC,
        SetToolbarIconSizeRPC,
        SaveViewRPC,
        SetGlobalLineoutAttributesRPC,
        SetPickAttributesRPC,
        ExportColorTableRPC,
        ExportEntireStateRPC,
        ImportEntireStateRPC,
        ImportEntireStateWithDifferentSourcesRPC,
        ResetPickAttributesRPC,
        AddAnnotationObjectRPC,
        HideActiveAnnotationObjectsRPC,
        DeleteActiveAnnotationObjectsRPC,
        RaiseActiveAnnotationObjectsRPC,
        LowerActiveAnnotationObjectsRPC,
        SetAnnotationObjectOptionsRPC,
        SetDefaultAnnotationObjectListRPC,
        ResetAnnotationObjectListRPC,
        ResetPickLetterRPC,
        SetDefaultPickAttributesRPC,
        ChooseCenterOfRotationRPC,
        SetCenterOfRotationRPC,
        SetQueryOverTimeAttributesRPC,
        SetDefaultQueryOverTimeAttributesRPC,
        ResetQueryOverTimeAttributesRPC,
        ResetLineoutColorRPC,
        SetInteractorAttributesRPC,
        SetDefaultInteractorAttributesRPC,
        ResetInteractorAttributesRPC,
        GetProcInfoRPC,
        SendSimulationCommandRPC,
        UpdateDBPluginInfoRPC,
        ExportDBRPC,
        SetTryHarderCyclesTimesRPC,
        OpenClientRPC,
        OpenGUIClientRPC,
        OpenCLIClientRPC,
        SuppressQueryOutputRPC,
        SetQueryFloatFormatRPC,
        SetMeshManagementAttributesRPC,
        SetDefaultMeshManagementAttributesRPC,
        ResetMeshManagementAttributesRPC,
        ResizeWindowRPC,
        MoveWindowRPC,
        MoveAndResizeWindowRPC,
        SetStateLoggingRPC,
        ConstructDataBinningRPC,
        RequestMetaDataRPC,
        SetTreatAllDBsAsTimeVaryingRPC,
        SetCreateMeshQualityExpressionsRPC,
        SetCreateTimeDerivativeExpressionsRPC,
        SetCreateVectorMagnitudeExpressionsRPC,
        SetPrecisionTypeRPC,
        CopyActivePlotsRPC,
        SetPlotFollowsTimeRPC,
        TurnOffAllLocksRPC,
        SetDefaultFileOpenOptionsRPC,
        SetSuppressMessagesRPC,
        ApplyNamedSelectionRPC,
        CreateNamedSelectionRPC,
        DeleteNamedSelectionRPC,
        LoadNamedSelectionRPC,
        SaveNamedSelectionRPC,
        SetNamedSelectionAutoApplyRPC,
        UpdateNamedSelectionRPC,
        InitializeNamedSelectionVariablesRPC,
        MenuQuitRPC,
        SetPlotDescriptionRPC,
        MovePlotOrderTowardFirstRPC,
        MovePlotOrderTowardLastRPC,
        SetPlotOrderToFirstRPC,
        SetPlotOrderToLastRPC,
        RenamePickLabelRPC,
        GetQueryParametersRPC,
        DDTConnectRPC,
        DDTFocusRPC,
        ReleaseToDDTRPC,
        PlotDDTVispointVariablesRPC,
        ExportRPC,
        MaxRPC
	}
}