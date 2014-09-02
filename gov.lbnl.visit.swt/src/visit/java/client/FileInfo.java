/**
 * 
 */
package visit.java.client;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hari
 *
 */
public class FileInfo {
    
    private String filename;
    private String filetype;
    private String description;
    
    private List<String> scalars;
    private List<String> vectors;
    private List<String> materials;
    private List<String> meshes;

    public FileInfo() {
        filename = "";
        filetype = "";
        description = "";
        
        scalars = new ArrayList<String>();
        vectors = new ArrayList<String>();
        materials = new ArrayList<String>();
        meshes = new ArrayList<String>();
    }
    
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
    
    public void setScalars(List<String> v) {
        scalars.clear();
        scalars.addAll(v);
    }
    
    public List<String> getScalars() {
        return scalars;
    }

    public void setVectors(List<String> v) {
        vectors.clear();
        vectors.addAll(v);
    }

    public List<String> getVectors() {
        return vectors;
    }
    
    public void setMaterials(List<String> v) {
        materials.clear();
        materials.addAll(v);
    }
    
    public List<String> getMaterials() {
        return materials;
    }
    
    public void setMeshes(List<String> m) {
        meshes.clear();
        meshes.addAll(m);
    }
    
    public List<String> getMeshes() {
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
