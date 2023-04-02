package action;

public abstract class AbstractAction {
  
  public abstract boolean canPerform();
  public abstract boolean perform();
  public abstract boolean requiresInput();
}