package com.neu.prattle.exceptions;

public class NotExistingMember extends RuntimeException{
  private static final long serialVersionUID = -4845176561270017896L;
  public NotExistingMember(String s) {
    super(s);
  }
}
