package eg.edu.guc.csen.transpiler;

public class TranspilerException extends Exception{
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public TranspilerException() {
        super();
    }

    public TranspilerException(String message) {
        super(message);
    }

    public TranspilerException(String message, Throwable cause) {
        super(message, cause);
    }
}
