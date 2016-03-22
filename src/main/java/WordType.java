
public enum WordType {

    SURFACE_ALL("surface_all"),
    SURFACE_NO_PM("surface_no_pm"),
    STEM("stem"),
    SUFFIX_X("suffix_x");

    private String wordType;

    private WordType(String wordType) {
        this.wordType = wordType;
    }

    private String getTypeValue() {
        return wordType;
    }
    
    static public WordType getType(String pType) {
        for (WordType type : WordType.values()) {
            if (type.getTypeValue().equals(pType)) {
                return type;
            }
        }
        throw new RuntimeException("unknown type");
    }
}
