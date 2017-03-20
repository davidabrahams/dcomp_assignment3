import java.io.Serializable;

class NameIP implements Serializable, Comparable<NameIP>{
    String name, ip;
    NameIP(String name, String ip){
        this.name = name;
        this.ip = ip;
    }
    @Override public boolean equals(Object other) {
        return (other instanceof NameIP) && ((NameIP) other).compareTo(this)==0;
    }

    @Override
    public int compareTo(NameIP o) {
        if (o.ip.compareTo(this.ip) != 0) {
            return o.ip.compareTo(this.ip);
        }
        return o.name.compareTo(this.name);
    }

    @Override
    public String toString() {
        return "Name: " + name + ", IP: " + ip;
    }

    @Override
    public int hashCode () {
        return name.hashCode() + ip.hashCode();
    }
}
