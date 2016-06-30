/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/
 */
package de.monticore.lang.montiarc.symtab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import de.monticore.lang.montiarc.AbstractSymtabTest;
import de.monticore.lang.montiarc.common._ast.ASTParameter;
import de.monticore.lang.montiarc.montiarc._symboltable.ComponentSymbol;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.types.JAttributeSymbol;
import de.se_rwth.commons.logging.Log;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 * @since TODO: add version number
 */
public class DefaultParametersTest extends AbstractSymtabTest {

  public static final boolean ENABLE_FAIL_QUICK = true;

  @BeforeClass
  public static void setUp() {
    // ensure an empty log
    Log.getFindings().clear();
    Log.enableFailQuick(ENABLE_FAIL_QUICK);
  }

  @Test
  public void testSubcomponentWithInstanceName() {
    Scope symTab = createSymTab("src/test/resources/arc4/symtab");
    ComponentSymbol comp = symTab.<ComponentSymbol>resolve(
        "features.DefaultParameters", ComponentSymbol.KIND).orElse(null);
    assertNotNull(comp);
    List<JAttributeSymbol> params = comp.getConfigParameters();
    for (JAttributeSymbol param : params) {
      if (param.getAstNode().isPresent()) {
        ASTParameter p = (ASTParameter) param.getAstNode().get();
        if (p.getName().equals("offset")) {
          assertTrue(p.getDefaultValue().isPresent());
          assertEquals(5, p.getDefaultValue().get());
        }
        else {
          assertFalse(p.getDefaultValue().isPresent());
        }
      }

    }
  }
}
