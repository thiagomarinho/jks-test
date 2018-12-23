package net.thiagomarinho.jkstest;

public class ApplicationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ApplicationException(Exception e) {
        super(e);
    }

}
