parser grammar CoolParser;
options {
    tokenVocab = CoolLexer;
}
@header{
    package cool.parser;
}

formal : name=ID COLON type=TYPE;

feature : name=ID LPAREN (formals+=formal(COMMA formals+=formal)*)? RPAREN COLON type=TYPE LBRACE e=expr RBRACE # method
                | name=ID COLON type=TYPE (ASSIGN e=expr)? #atribut
                ;

classRule : CLASS type=TYPE (INHERITS inheritsType=TYPE)? LBRACE (body+=feature SEMI)* RBRACE;

letVar :  name=ID COLON type=TYPE (ASSIGN e=expr)?;
caseBranch : name=ID COLON type=TYPE APPLY e=expr;

program : (classes+=classRule SEMI)+ EOF;


expr :   e1=expr (AT type=TYPE)? DOT name=ID LPAREN (args+=expr (COMMA args+=expr)*)? RPAREN    # callMethod
       | name=ID LPAREN (e+=expr (COMMA e+=expr)*)? RPAREN                                      # callFunction
       | IF cond=expr THEN thenBranch=expr ELSE elseBranch=expr FI                              # if
       | WHILE cond=expr LOOP e=expr POOL                                                       # while
       | LBRACE (e+=expr SEMI)+ RBRACE                                                          # block
       | LET defs+=letVar (COMMA defs+=letVar)* IN body=expr                                    # let
       | CASE cond=expr OF (branches+=caseBranch SEMI)+ ESAC                                    # case
       | NEW type=TYPE                                                                          # new
       | ISVOID e=expr                                                                          # isVoid
       | left=expr op=(MULT | DIV) right=expr                                                   # multDiv
       | left=expr op=(PLUS | MINUS) right=expr                                                 # plusMinus
       | NEGATIVE e=expr                                                                        # unaryMinus
       | left=expr op=(LT | LE | EQUAL) right=expr                                              # relational
       | NOT e=expr                                                                             # not
       | LPAREN e=expr RPAREN                                                                   # parantheses
       | ID                                                                                     # id
       | INT                                                                                    # int
       | STRING                                                                                 # string
       | BOOL                                                                                   # bool
       | name=ID ASSIGN e=expr                                                                  # assign
       ;