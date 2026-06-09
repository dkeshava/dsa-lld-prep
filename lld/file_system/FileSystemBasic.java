import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class FileSystemNode{
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    public FileSystemNode(String name){
        this.name=name;
        this.createdAt=LocalDateTime.now();
        this.modifiedAt=LocalDateTime.now();
    }
    
    public abstract boolean isFile();
    public abstract void display(int depth);
    public String getName(){
        return name;
    }
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getModifiedAt(){return modifiedAt;}
}

class File extends FileSystemNode{
    private String content;
    private String extension;
    public File(String name){
        super(name);
        this.extension=extractExtension(name);
    }
    private String extractExtension(String name){
        int dotIndex=name.lastIndexOf('.');
        return (dotIndex > 0) ? name.substring(dotIndex + 1) : "";
    }
    public void setContent(String content){
        this.content=content;
    }
    public String getContent(){return content;}
    @Override
    public boolean isFile(){return true;}
    @Override
    public void display(int depth){
        String indent = " ".repeat(depth * 2);
        System.out.println(indent + "📄 " + getName());
    }
}

class Directory extends FileSystemNode{
    private LinkedHashMap<String, FileSystemNode> children;
    public Directory(String name){
        super(name);
        this.children=new LinkedHashMap<>();
    }
    public void addChild(String name, FileSystemNode node){
        if(children.containsKey(name)){
            throw new RuntimeException("Already exists");
        }
        children.put(name,node);
    }
    public boolean hasChild(String name){
        return children.containsKey(name);
    }
    public FileSystemNode getChild(String name){
        if(!hasChild(name)) return null;
        return children.get(name);
    }
    public boolean removeChild(String name){
        return children.remove(name)!=null;
    }
    @Override
    public boolean isFile(){return false;}
    @Override
    public void display(int depth){
        String indent = " ".repeat(depth*2);
        System.out.println(indent + "📁 " + getName() + " (" + getChildren().size() + " items)");
        for(FileSystemNode node : children.values()){
            node.display(depth+1);
        }
    }
    public Map<String , FileSystemNode> getChildren(){return children;}
}

class FileSystem{
    private Directory root;
    public FileSystem(){
        this.root=new Directory("/");
    }
    public void mkdir(String name){
        root.addChild(name,new Directory(name));
    }
    public void addFile(String name){
        root.addChild(name, new File(name));
    }
    public void deleteFile(String name){
        root.removeChild(name);
    }
    public void ls(){
        root.display(0);
    }
}
public class FileSystemBasic {
    public static void main(String[] args) {
        FileSystem fs=new FileSystem();
        fs.mkdir("documents");
        fs.mkdir("photos");

        fs.addFile("resume.pdf");
        fs.addFile("notes.txt");
        fs.ls();
    }
}
