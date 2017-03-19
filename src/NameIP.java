import java.io.Serializable;

class NameIP implements Serializable {
    String name, ip;
    NameIP(String name, String ip){
        this.name = name;
        this.ip = ip;
    }
    @Override public boolean equals(Object other) {
        return (other instanceof NameIP) && ((NameIP) other).name.equals(name) && ((NameIP) other).ip.equals(ip);
    }

}
