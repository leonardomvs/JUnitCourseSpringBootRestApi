package io.github.leonardomvs.springmvc.exceptionhandling;

public class StudentOrGradeNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -4680778595174257786L;

	public StudentOrGradeNotFoundException(String message) {
        super(message);
    }

    public StudentOrGradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentOrGradeNotFoundException(Throwable cause) {
        super(cause);
    }
}
