package cool.compiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import cool.lexer.*;
import cool.parser.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;


public class Compiler {
    // Annotates class nodes with the names of files where they are defined.
    public static ParseTreeProperty<String> fileNames = new ParseTreeProperty<>();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("No file(s) given");
            return;
        }
        
        CoolLexer lexer = null;
        CommonTokenStream tokenStream = null;
        CoolParser parser = null;
        ParserRuleContext globalTree = null;
        
        // True if any lexical or syntax errors occur.
        boolean lexicalSyntaxErrors = false;
        
        // Parse each input file and build one big parse tree out of
        // individual parse trees.
        for (var fileName : args) {
            var input = CharStreams.fromFileName(fileName);
            
            // Lexer
            if (lexer == null)
                lexer = new CoolLexer(input);
            else
                lexer.setInputStream(input);

            // Token stream
            if (tokenStream == null)
                tokenStream = new CommonTokenStream(lexer);
            else
                tokenStream.setTokenSource(lexer);
                

            // Test lexer only.
           /* tokenStream.fill();
            List<Token> tokens = tokenStream.getTokens();
            tokens.stream().forEach(token -> {
                var text = token.getText();
                var name = CoolLexer.VOCABULARY.getSymbolicName(token.getType());
                
                System.out.println(text + " : " + name);
                //System.out.println(token);
            });*/

            
            // Parser
            if (parser == null)
                parser = new CoolParser(tokenStream);
            else
                parser.setTokenStream(tokenStream);
            
            // Customized error listener, for including file names in error
            // messages.
            var errorListener = new BaseErrorListener() {
                public boolean errors = false;
                
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer,
                                        Object offendingSymbol,
                                        int line, int charPositionInLine,
                                        String msg,
                                        RecognitionException e) {
                    String newMsg = "\"" + new File(fileName).getName() + "\", line " +
                                        line + ":" + (charPositionInLine + 1) + ", ";
                    
                    Token token = (Token)offendingSymbol;
                    if (token.getType() == CoolLexer.ERROR)
                        newMsg += "Lexical error: " + token.getText();
                    else
                        newMsg += "Syntax error: " + msg;
                    
                    System.err.println(newMsg);
                    errors = true;
                }
            };
            
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Actual parsing
            var tree = parser.program();
            if (globalTree == null)
                globalTree = tree;
            else
                // Add the current parse tree's children to the global tree.
                for (int i = 0; i < tree.getChildCount(); i++)
                    globalTree.addAnyChild(tree.getChild(i));
                    
            // Annotate class nodes with file names, to be used later
            // in semantic error messages.
            for (int i = 0; i < tree.getChildCount(); i++) {
                var child = tree.getChild(i);
                // The only ParserRuleContext children of the program node
                // are class nodes.
                if (child instanceof ParserRuleContext)
                    fileNames.put(child, fileName);
            }
            
            // Record any lexical or syntax errors.
            lexicalSyntaxErrors |= errorListener.errors;
        }

        // Stop before semantic analysis phase, in case errors occurred.
        if (lexicalSyntaxErrors) {
            System.err.println("Compilation halted");
            return;
        }
        
        // TODO Print tree
        var astConstructionVisitor = new CoolParserBaseVisitor<ASTNode>() {
            @Override
            public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
                LinkedList<ClassRule> classes = new LinkedList<>();
                for (var cl : ctx.classes) {
                    classes.add((ClassRule)visit(cl));
                }
                return new Prog(classes, ctx.start);
            }

            @Override
            public ASTNode visitParantheses(CoolParser.ParanthesesContext ctx) {
                return new Parantheses((Expression)visit(ctx.e),ctx.start);
            }

            @Override
            public ASTNode visitUnaryMinus(CoolParser.UnaryMinusContext ctx) {
                return new UnaryMinus((Expression)visit(ctx.e), ctx.start);
            }

            @Override
            public ASTNode visitClassRule(CoolParser.ClassRuleContext ctx) {
                LinkedList<Feature> features = new LinkedList<>();
                if (ctx.body != null) {
                    for (var f : ctx.body)
                        features.add((Feature) visit(f));
                }

                return new ClassRule(ctx.type, ctx.inheritsType, features, ctx.start);
            }

            @Override
            public ASTNode visitFormal(CoolParser.FormalContext ctx) {
                return new Formal(ctx.name, ctx.type, ctx.start);
            }

            @Override
            public ASTNode visitMethod(CoolParser.MethodContext ctx) {
                Expression expr = null;
                LinkedList<Formal> formals =  new LinkedList<>();
                if (ctx.e != null)
                    expr = (Expression)visit(ctx.e);
                if (ctx.formals != null) {
                    for(var f : ctx.formals) {
                        formals.add((Formal)visit(f));
                    }
                }
                return new Method(ctx.name, formals, ctx.type, expr, ctx.start);
            }

            @Override
            public ASTNode visitBool(CoolParser.BoolContext ctx) {
                return new Bool(ctx.start);
            }

            @Override
            public ASTNode visitAtribut(CoolParser.AtributContext ctx) {
                Expression expr = null;
                if (ctx.e != null)
                    expr = (Expression)visit(ctx.e);
                return new Atribut(ctx.name, ctx.type, expr, ctx.start);
            }

            @Override
            public ASTNode visitInt(CoolParser.IntContext ctx) {
                return new Int(ctx.start);
            }

            @Override
            public ASTNode visitId(CoolParser.IdContext ctx) {
                return new Id(ctx.start);
            }

            @Override
            public ASTNode visitString(CoolParser.StringContext ctx) {
                return new Str(ctx.start);
            }

            @Override
            public ASTNode visitPlusMinus(CoolParser.PlusMinusContext ctx) {
                return new PlusMinus((Expression)visit(ctx.left), ctx.op, (Expression)visit(ctx.right), ctx.start);
            }

            @Override
            public ASTNode visitMultDiv(CoolParser.MultDivContext ctx) {
                return new MultDiv((Expression)visit(ctx.left),ctx.op,(Expression)visit(ctx.right), ctx.start);
            }

            @Override
            public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
                return new Relational((Expression)visit(ctx.left), ctx.op, (Expression)visit(ctx.right), ctx.start);
            }

            @Override
            public ASTNode visitNot(CoolParser.NotContext ctx) {
                return new Not((Expression)visit(ctx.e), ctx.start);
            }

            @Override
            public ASTNode visitAssign(CoolParser.AssignContext ctx) {
                return new Assign(ctx.name, (Expression)visit(ctx.e), ctx.start);
            }

            @Override
            public ASTNode visitIsVoid(CoolParser.IsVoidContext ctx) {
                return new IsVoid((Expression)visit(ctx.e), ctx.start);
            }

            @Override
            public ASTNode visitNew(CoolParser.NewContext ctx) {
                return new New(ctx.type, ctx.start);
            }

            @Override
            public ASTNode visitIf(CoolParser.IfContext ctx) {
                return new If((Expression)visit(ctx.cond), (Expression)visit(ctx.thenBranch),
                        (Expression)visit(ctx.elseBranch), ctx.start);
            }

            @Override
            public ASTNode visitLet(CoolParser.LetContext ctx) {
                LinkedList<LetVar> defs = new LinkedList<>();
                for (var e : ctx.defs) {
                    defs.add((LetVar)visit(e));
                }
                return new Let(defs, (Expression)visit(ctx.body), ctx.start);
            }

            @Override
            public ASTNode visitWhile(CoolParser.WhileContext ctx) {
                return new While((Expression)visit(ctx.cond), (Expression)visit(ctx.e), ctx.start);
            }

            @Override
            public ASTNode visitBlock(CoolParser.BlockContext ctx) {
                LinkedList<Expression> expr = new LinkedList<>();
                for (var e : ctx.e) {
                    expr.add((Expression)visit(e));
                }
                return new Block(expr, ctx.start);
            }

            @Override
            public ASTNode visitCallMethod(CoolParser.CallMethodContext ctx) {
                LinkedList<Expression> args = new LinkedList<>();
                for(var e : ctx.args) {
                    args.add((Expression)visit(e));
                }
                return new CallMethod((Expression)visit(ctx.e1), ctx.name, ctx.type, args, ctx.start);
            }

            @Override
            public ASTNode visitCaseBranch(CoolParser.CaseBranchContext ctx) {
                return new CaseBranch(ctx.name, ctx.type, (Expression)visit(ctx.e));
            }

            @Override
            public ASTNode visitCase(CoolParser.CaseContext ctx) {
                LinkedList<CaseBranch> branches = new LinkedList<>();
                for(var b : ctx.caseBranch()) {
                    branches.add((CaseBranch)visit(b));
                }
                return new Case((Expression)visit(ctx.cond), branches, ctx.start);
            }

            @Override
            public ASTNode visitCallFunction(CoolParser.CallFunctionContext ctx) {
                LinkedList<Expression> expr = new LinkedList<>();
                if (ctx.e != null)
                    for (var e : ctx.e) {
                        expr.add((Expression)visit(e));
                    }
                return new CallFunction(ctx.name, expr, ctx.start);
            }

            @Override
            public ASTNode visitLetVar(CoolParser.LetVarContext ctx) {
                Expression expr = null;
                if (ctx.e != null) {
                    expr = (Expression)visit(ctx.e);
                }
                return new LetVar(ctx.name, ctx.type, expr);
            }
        };
        var ast = astConstructionVisitor.visit(globalTree);
        var printVisitor = new ASTVisitor<Void>() {
            int indent = 0;
            @Override
            public Void visit(Prog prog) {
                printIndent("program");
                indent++;
                for (var cl : prog.classes) {
                    cl.accept(this);
                }
                indent--;
                return null;
            }

            @Override
            public Void visit(ClassRule cl) {
                printIndent("class");
                indent++;
                printIndent(cl.type.getText());
                if (cl.inheritsType != null)
                    printIndent(cl.inheritsType.getText());
                if (cl.body != null) {
                    for (var f : cl.body)
                        f.accept(this);
                }
                indent--;
                return null;
            }

            @Override
            public Void visit(Formal formal) {
                printIndent("formal");
                indent++;
                printIndent(formal.name.getText());
                printIndent(formal.type.getText());
                indent--;
                return null;
            }

            @Override
            public Void visit(Method method) {
                printIndent("method");
                indent++;
                printIndent(method.name.getText());
                for(var f : method.formals) {
                    f.accept(this);
                }
                printIndent(method.type.getText());
                if (method.expr != null)
                    method.expr.accept(this);
                indent--;
                return  null;
            }

            @Override
            public Void visit(Atribut atribut) {
                printIndent("attribute");
                indent++;
                printIndent(atribut.name.getText());
                printIndent(atribut.type.getText());
                if (atribut.expr != null)
                    atribut.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Int integer) {
                printIndent(integer.token.getText());
                return null;
            }

            @Override
            public Void visit(Bool bool) {
                printIndent(bool.token.getText());
                return null;
            }

            @Override
            public Void visit(Str str) {
                printIndent(str.token.getText());
                return null;
            }

            @Override
            public Void visit(Id id) {
                printIndent(id.token.getText());
                return null;
            }

            @Override
            public Void visit(MultDiv multDiv) {
                printIndent(multDiv.op.getText());
                indent++;
                multDiv.left.accept(this);
                multDiv.right.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(PlusMinus plusMinus) {
                printIndent(plusMinus.sign.getText());
                indent++;
                plusMinus.left.accept(this);
                plusMinus.right.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Parantheses parantheses) {
                parantheses.expr.accept(this);
                return null;
            }

            @Override
            public Void visit(UnaryMinus unaryMinus) {
                printIndent("~");
                indent++;
                unaryMinus.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Relational relational) {
                printIndent(relational.op.getText());
                indent++;
                relational.left.accept(this);
                relational.right.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Not not) {
                printIndent("not");
                indent++;
                not.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Assign assign) {
                printIndent("<-");
                indent++;
                printIndent(assign.name.getText());
                assign.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(IsVoid isVoid) {
                printIndent("isvoid");
                indent++;
                isVoid.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(New newRule) {
                printIndent("new");
                indent++;
                printIndent(newRule.type.getText());
                indent--;
                return null;
            }

            @Override
            public Void visit(If ifRule) {
                printIndent("if");
                indent++;
                ifRule.cond.accept(this);
                ifRule.thenBranch.accept(this);
                ifRule.elseBranch.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(While whileRule) {
                printIndent("while");
                indent++;
                whileRule.cond.accept(this);
                whileRule.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Block block) {
                printIndent("block");
                indent++;
                for(var e : block.expr) {
                    e.accept(this);
                }
                indent--;
                return null;
            }

            @Override
            public Void visit(CallMethod callMethod) {
                printIndent(".");
                indent++;
                callMethod.expr.accept(this);
                if (callMethod.type != null)
                    printIndent(callMethod.type.getText());
                printIndent(callMethod.name.getText());
                for (var arg : callMethod.args) {
                    arg.accept(this);
                }
                indent--;

                return null;
            }

            @Override
            public Void visit(CallFunction callFunction) {
                printIndent("implicit dispatch");
                indent++;
                printIndent(callFunction.name.getText());
                for (var e : callFunction.expr) {
                    e.accept(this);
                }
                indent--;
                return null;
            }

            @Override
            public Void visit(LetVar letVar) {
                printIndent("local");
                indent++;
                printIndent(letVar.name.getText());
                printIndent(letVar.type.getText());
                if(letVar.expr != null)
                    letVar.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Let letRule) {
                printIndent("let");
                indent++;
                for (var d : letRule.defs) {
                    d.accept(this);
                }
                if (letRule.body != null)
                    letRule.body.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(CaseBranch caseBranch) {
                printIndent("case branch");
                indent++;
                printIndent(caseBranch.name.getText());
                printIndent(caseBranch.type.getText());
                caseBranch.expr.accept(this);
                indent--;
                return null;
            }

            @Override
            public Void visit(Case caseRule) {
                printIndent("case");
                indent++;
                caseRule.cond.accept(this);
                for (var b : caseRule.branches) {
                    b.accept(this);
                }
                indent--;
                return null;
            }


            void printIndent(String str) {
                for (int i = 0; i < indent; i++)
                    System.out.print("  ");
                System.out.println(str);
            }
        };
        ast.accept(printVisitor);
    }
}
