/* generated from model null*/
/* generated by template templates.de.monticore.lang.montiarc.tagschema.ValuedTagType*/


package nfp.PowerConsumptionTagSchema;

import de.monticore.lang.montiarc.tagging._symboltable.TagKind;
import de.monticore.lang.montiarc.tagging._symboltable.TagSymbol;


/**
 * Created by ValuedTagType.ftl
 */
public class PowerBooleanSymbol extends TagSymbol {
  public static final PowerBooleanKind KIND = PowerBooleanKind.INSTANCE;

  public PowerBooleanSymbol(Boolean value) {
    super(KIND, value);
  }

  protected PowerBooleanSymbol(PowerBooleanKind kind, Boolean value) {
    super(kind, value);
  }

  public Boolean getValue() {
     return getValue(0);
  }

  @Override
  public String toString() {
    return String.format("PowerBoolean = %s",
      getValue().toString());
  }

  public static class PowerBooleanKind extends TagKind {
    public static final PowerBooleanKind INSTANCE = new PowerBooleanKind();

    protected PowerBooleanKind() {
    }
  }
}