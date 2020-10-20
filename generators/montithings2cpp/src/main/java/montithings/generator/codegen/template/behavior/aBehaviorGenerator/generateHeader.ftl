${tc.signature("comp","compname")}
<#assign generics = Utils.printFormalTypeParameters(comp, false)>
#pragma once
#include "${compname}Input.h"
#include "${compname}Result.h"
#include "IComputable.h"
#include ${"<stdexcept>"}
${Utils.printNamespaceStart(comp)}

${Utils.printTemplateArguments(comp)}
class ${compname}Impl : public IComputable
<${compname}Input${generics},${compname}Result${generics}>{ {
protected:
${Utils.printVariables(comp)}
${Utils.printConfigParameters(comp)}


public:
${hook(comp, compname)}
${printConstructor(comp, compname)}
virtual ${compname}Result${generics} getInitialValues() override;
virtual ${compname}Result${generics} compute(${compname}Input${generics} input) override;

};

<#if comp.hasTypeParameter()>
    ${generateBody(comp, compname)}
</#if>
${Utils.printNamespaceEnd(comp)}