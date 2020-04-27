package com.neu.prattle.exceptions;

public class AlreadyExistingModeratorException extends RuntimeException{
  private static final long serialVersionUID = -4845176561270017896L;
  public AlreadyExistingModeratorException(String s) {
    super(s);
  }
}
