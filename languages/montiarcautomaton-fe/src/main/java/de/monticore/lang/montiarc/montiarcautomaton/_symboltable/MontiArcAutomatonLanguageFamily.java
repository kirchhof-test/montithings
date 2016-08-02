package de.monticore.lang.montiarc.montiarcautomaton._symboltable;

import de.monticore.ModelingLanguageFamily;
import de.monticore.automaton.ioautomatonjava._symboltable.IOAutomatonJavaLanguage;
import de.monticore.java.lang.JavaDSLLanguage;
import de.monticore.lang.montiarc.montiarc._symboltable.MontiArcLanguage;
import de.monticore.lang.montiarc.montiarc._symboltable.MontiArcLanguageFamily;
import de.monticore.umlcd4a.CD4AnalysisLanguage;

public class MontiArcAutomatonLanguageFamily extends ModelingLanguageFamily {
  public MontiArcAutomatonLanguageFamily() {
//    addModelingLanguage(new MontiArcLanguage());
    addModelingLanguage(new MontiArcAutomatonLanguage());
    addModelingLanguage(new CD4AnalysisLanguage());
    addModelingLanguage(new JavaDSLLanguage());
    addModelingLanguage(new IOAutomatonJavaLanguage());
  }
}
