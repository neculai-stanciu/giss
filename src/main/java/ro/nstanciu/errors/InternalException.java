package ro.nstanciu.errors;

public class InternalException extends RuntimeException {
  private static final long serialVersionUID = 4006820565169315656L;
  private String message;

  public InternalException(String message, Throwable e) {
    super(message, e);
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

}
