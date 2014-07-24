/**
 * 
 */
package gov.lbnl.visit.swt;

import java.util.ArrayList;

/**
 * @author hari
 *
 */
public class FileInfo {
	
	private String filename = "";
	private String filetype = "";
	private String description = "";
	
	private ArrayList<String> scalars = new ArrayList<String>();
	private ArrayList<String> vectors = new ArrayList<String>();
	private ArrayList<String> materials = new ArrayList<String>();
	private ArrayList<String> meshes = new ArrayList<String>();

	public void setFileName(String file) {
		filename = file;
	}
	
	public String getFileName() {
		return filename;
	}
	
	public void setFileType(String filet) {
		filetype = filet;
	}
	
	public String getFileType() {
		return filetype;
	}
	
	public void setFileDescription(String desc) {
		description = desc;
	}
	
	public String getFileDescription() {
		return description;
	}
	
	public void setScalars(ArrayList<String> v) {
		scalars.clear();
		scalars.addAll(v);
	}
	
	public ArrayList<String> getScalars() {
		return scalars;
	}

	public void setVectors(ArrayList<String> v) {
		vectors.clear();
		vectors.addAll(v);
	}

	public ArrayList<String> getVectors() {
		return vectors;
	}
	
	public void setMaterials(ArrayList<String> v) {
		materials.clear();
		materials.addAll(v);
	}
	
	public ArrayList<String> getMaterials() {
		return materials;
	}
	
	public void setMeshes(ArrayList<String> v) {
		meshes.clear();
		meshes.addAll(v);
	}
	
	public ArrayList<String> getMeshes() {
		return meshes;
	}
	
	/**
	 * 
	 */
	public String toString() {

		String result = "";

		result = filename + " " + filetype + " " + description + "\n";

		result += "Meshes: " + meshes.toString() + "\n";
		result += "Scalars: " + scalars.toString() + "\n";
		result += "Materials: " + materials.toString() + "\n";
		result += "Vectors: " + vectors.toString() + "\n";
		return result;
	}
}
