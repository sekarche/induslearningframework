package airldm2.exceptions;

public class ArffReadingException extends Exception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 3L;
    // TODO: A better implementation on lines of Exceptions

    int code;

    public ArffReadingException(int code, String message) {
	super(message);
	this.code = code;
    }

    public int getCode() {
	return code;
    }

}
