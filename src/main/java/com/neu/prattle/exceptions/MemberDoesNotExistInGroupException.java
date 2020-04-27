package com.neu.prattle.exceptions;

public class MemberDoesNotExistInGroupException extends RuntimeException{
  private static final long serialVersionUID = -4845176561270017896L;

  public MemberDoesNotExistInGroupException(String message) {
    super(message);
  }
}
