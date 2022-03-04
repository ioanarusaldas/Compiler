package cool.compiler;

import java.beans.Expression;

public interface ASTVisitor<T> {
    T visit(Prog prog);
    T visit(ClassRule cl);
    T visit(Formal formal);
    T visit(Method method);
    T visit(Atribut atribut);
    T visit(Int integer);
    T visit(Bool bool);
    T visit(Str str);
    T visit(Id id);
    T visit(MultDiv multDiv);
    T visit(PlusMinus plusMinus);
    T visit(Parantheses parantheses);
    T visit(UnaryMinus unaryMinus);
    T visit(Relational relational);
    T visit(Not not);
    T visit(Assign assign);
    T visit(IsVoid isVoid);
    T visit(New newRule);
    T visit(If ifRule);
    T visit(While whileRule);
    T visit(Block block);
    T visit(CallMethod callMethod);
    T visit(CallFunction callFunction);
    T visit(LetVar letVar);
    T visit(Let letRule);
    T visit(CaseBranch caseBranch);
    T visit(Case caseRule);


}
