package ec.edu.espe.zonas.entidades;

public enum TipoZona {

    VIP("VIP"),
    VISITANTES("VIS"),
    GENERAL("GEN"),
    PREFERENCIAL("PRE");

    // Abreviatura de 3 letras para generar el código de zona: ZON-VIS-01, ZON-GEN-02
    private final String abreviatura;

    TipoZona(String abreviatura) {
        this.abreviatura = abreviatura;
    }

    public String getAbreviatura() {
        return abreviatura;
    }
}

