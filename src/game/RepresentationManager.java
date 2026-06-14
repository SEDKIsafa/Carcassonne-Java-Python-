package game;

import java.util.HashMap;

public class RepresentationManager{


    private HashMap<String,Integer> tuilesRepresentation;

    public RepresentationManager(){
        this.tuilesRepresentation=new HashMap<>();
    }

    public HashMap<String,Integer> getTuilesRepresentations(){
        return this.tuilesRepresentation;
    }


    public void initTuilesRepresentation(){


        this.tuilesRepresentation.put("f-f-frf-f:A",2);
        this.tuilesRepresentation.put("f-f-f-f:A",4);
        this.tuilesRepresentation.put("C-C-C-C",1);
        this.tuilesRepresentation.put("c-f0rf1-f1-f1rf0",3);
        this.tuilesRepresentation.put("c-f-f-f",5);

        this.tuilesRepresentation.put("f0-C-f1-C",2);
        this.tuilesRepresentation.put("f0-c-f1-c",1);
        this.tuilesRepresentation.put("c0-f-c1-f",3);
        this.tuilesRepresentation.put("c0-f-f-c1",2);
        this.tuilesRepresentation.put("c-f0rf1-f1rf0-f0",3);

        this.tuilesRepresentation.put("c-f0-f0rf1-f1rf0",3);
        this.tuilesRepresentation.put("c-f0r0f1-f1r1f2-f2r2f0",3);
        this.tuilesRepresentation.put("C-C-f-f",2);
        this.tuilesRepresentation.put("c-c-f-f",3);
        this.tuilesRepresentation.put("C-f0rf1-f1rf0-C",2);
        
        this.tuilesRepresentation.put("c-f0rf1-f1rf0-c",3);
        this.tuilesRepresentation.put("C-C-f-C",1);
        this.tuilesRepresentation.put("c-c-f-c",3);
        this.tuilesRepresentation.put("C-C-f0rf1-C",2);
        this.tuilesRepresentation.put("c-c-f0rf1-c",1);
        
        this.tuilesRepresentation.put("f0rf1-f1-f1rf0-f0",8);
        this.tuilesRepresentation.put("f0-f0-f0rf1-f1rf0",9);
        this.tuilesRepresentation.put("f0-f0r0f1-f1r1f2-f2r2f0",4);
        this.tuilesRepresentation.put("f0r0f1-f1r1f2-f2r2f3-f3r3f0",1);
    }
    
}
