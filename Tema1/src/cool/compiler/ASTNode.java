package cool.compiler;

import org.antlr.v4.runtime.Token;
import java.util.*;

// Rădăcina ierarhiei de clase reprezentând nodurile arborelui de sintaxă
// abstractă (AST). Singura metodă permite primirea unui visitor.
public abstract class ASTNode {
    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

class Prog extends ASTNode {
    // Reținem un token descriptiv al expresiei, pentru a putea afișa ulterior
    // informații legate de linia și coloana eventualelor erori semantice.
    Token token;
    List<ClassRule> classes;
    Prog(List<ClassRule> classes,Token token) {
        this.token = token;
        this.classes = classes;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class ClassRule extends ASTNode {
    Token token;
    Token type;
    Token inheritsType;
    LinkedList<Feature>body;

    ClassRule(Token type, Token inheritsType, LinkedList<Feature> body, Token token) {
        this.type = type;
        this.inheritsType = inheritsType;
        this.body = body;
        this.token = token;

    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
abstract class Feature extends ASTNode {
    Token token;

    Feature(Token token) {
        this.token = token;
    }
}
class  Method extends Feature {
    Token name;
    LinkedList<Formal> formals;
    Token type;
    Expression expr;
    Method(Token name, LinkedList<Formal> formals, Token type, Expression expr, Token token) {
        super(token);
        this.name = name;
        this.formals = formals;
        this.type = type;
        this.expr = expr;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Atribut extends Feature {
    Token name;
    Token type;
    Expression expr;
    Atribut(Token name, Token type, Expression expr, Token token) {
        super(token);
        this.name = name;
        this.type = type;
        this.expr = expr;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Formal extends ASTNode {
    Token token;
    Token name;
    Token type;

    Formal(Token name, Token type,Token token) {
        this.name = name;
        this.type = type;
        this.token = token;

    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
abstract class Expression extends ASTNode {
    Token token;
    Expression(Token token) {
        this.token = token;
    }
}
class Int extends Expression {
    Int(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Bool extends Expression {
    Bool(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Str extends Expression {
    Str(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Id extends Expression {
    Id(Token token) {
        super(token);
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class MultDiv extends Expression {
    Expression left;
    Token op;
    Expression right;

    MultDiv(Expression left, Token op, Expression right, Token start) {
        super(start);
        this.op = op;
        this.left = left;
        this.right = right;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class PlusMinus extends Expression {
    Expression left;
    Token sign;
    Expression right;

    PlusMinus(Expression left, Token sign, Expression right, Token start) {
        super(start);
        this.sign = sign;
        this.left = left;
        this.right = right;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Parantheses extends Expression {
    Expression expr;

    Parantheses(Expression expr, Token start) {
        super(start);
        this.expr = expr;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class UnaryMinus extends Expression {
    Expression expr;

    UnaryMinus(Expression expr, Token start) {
        super(start);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Relational extends Expression {
    Expression left;
    Token op;
    Expression right;
    Relational(Expression left, Token op, Expression right ,Token start) {
        super(start);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Not extends Expression {
    Expression expr;
    Not(Expression expr, Token start) {
        super(start);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Assign extends Expression {
    Token name;
    Expression expr;
    Assign(Token name, Expression expr, Token start) {
        super(start);
        this.name = name;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class IsVoid extends Expression {
    Expression expr;
    IsVoid(Expression expr, Token start) {
        super(start);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class New extends Expression {
    Token type;
    New(Token type, Token start) {
        super(start);
        this.type = type;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class If extends Expression {
    Expression cond;
    Expression thenBranch;
    Expression elseBranch;

    If(Expression cond,
       Expression thenBranch,
       Expression elseBranch,
       Token start) {
        super(start);
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class While extends Expression {
    Expression cond;
    Expression expr;

    While(Expression cond, Expression expr, Token start) {
        super(start);
        this.cond = cond;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Block extends Expression {
    LinkedList<Expression> expr;

    Block(LinkedList<Expression> expr, Token start) {
        super(start);
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class CallMethod extends Expression {
    Expression expr;
    Token type;
    Token name;
    LinkedList<Expression>args;

    CallMethod (Expression expr, Token name, Token type,LinkedList<Expression> args, Token start) {
        super(start);
        this.expr = expr;
        this.name = name;
        this.type = type;
        this.args = args;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class CallFunction extends Expression {
    Token name;
    LinkedList<Expression>expr;

    CallFunction (Token name, LinkedList<Expression> expr, Token start) {
        super(start);
        this.name = name;
        this.expr = expr;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class LetVar extends ASTNode{
    Token name;
    Token type;
    Expression expr;

    public LetVar(Token name, Token type, Expression expr) {
        this.name = name;
        this.type = type;
        this.expr = expr;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Let extends Expression {
    LinkedList<LetVar> defs;
    Expression body;

    public Let(LinkedList<LetVar> defs, Expression body, Token start) {
        super(start);
        this.defs = defs;
        this.body = body;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class CaseBranch extends ASTNode{
    Token name;
    Token type;
    Expression expr;

    public CaseBranch(Token name, Token type, Expression expr) {
        this.name = name;
        this.type = type;
        this.expr = expr;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Case extends Expression {
    Expression cond;
    LinkedList<CaseBranch> branches;

    public Case(Expression cond, LinkedList<CaseBranch> branches, Token start) {
        super(start);
        this.cond = cond;
        this.branches = branches;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}