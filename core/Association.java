package core;

public interface Association {

    //for behavioral -- managing objects
    String getLabel();
    void setLabel(String label);
    int getId();
    void setId(int id);
    //for structural -- managing classes
    void update();
    


}
