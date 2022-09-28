package dam1.prog.tienda;

public class Roles {
    private static int id = 0;
    private String rol;

    public int getId() {
        return id;
    }

    public String getRol() {
        return this.rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Roles(String rol) {
        id++;
        this.rol = rol;
    }
}
    
