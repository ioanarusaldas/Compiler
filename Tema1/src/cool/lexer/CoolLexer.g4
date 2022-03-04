lexer grammar CoolLexer;

tokens { ERROR } 

@header{
    package cool.lexer;	
}

@members{    
    private void raiseError(String msg) {
        setText(msg);
        setType(ERROR);
    }
}


WS : [ \n\f\r\t]+ -> skip;

SEMI : ';';

CLASS : 'class';

INHERITS : 'inherits';

LBRACE : '{';

RBRACE : '}';

LPAREN : '(';

RPAREN : ')';

COMMA : ',';

COLON :  ':';

DOT : '.';

AT : '@';

ASSIGN : '<-';

IF : 'if';
THEN : 'then';
ELSE : 'else';
FI: 'fi';

WHILE : 'while';
LOOP : 'loop';
POOL : 'pool';

LET : 'let';
IN : 'in';

CASE : 'case';
OF : 'of';
ESAC : 'esac';
APPLY : '=>';

NEW : 'new';
ISVOID : 'isvoid';

BOOL : 'true' | 'false';

PLUS : '+';

MINUS : '-';

MULT : '*';

DIV : '/';

NEGATIVE : '~';

LT : '<';

LE : '<=';

EQUAL : '=';

NOT : 'not';

fragment SELF_TYPE : 'SELF_TYPE';
TYPE : 'Int' | 'String' | 'Bool' | 'IO' | 'Object' | CLASS_TYPE | SELF_TYPE;


fragment SMALL_LETTER : [a-z];
fragment HIGH_LETTER : [A-Z];
fragment LETTER : [a-zA-Z];
fragment DIGIT : [0-9];
ID : (SMALL_LETTER)(LETTER | '_' | DIGIT)*;
fragment CLASS_TYPE : (HIGH_LETTER)(LETTER | '_' | DIGIT)*;

INT : DIGIT+;

fragment NEW_LINE : '\r'? '\n';
STRING: '"' ('\\"' | '\\' NEW_LINE| .)*? (
	'"' {
        String str = getText();
        str = str.substring(1, str.length()-1);
        if (str.indexOf("\\t") != -1) {
            str = str.replace("\\t", "\t");
        }
        if (str.indexOf("\\n") != -1) {
            str = str.replace("\\n", "\n");
        }
        if (str.indexOf("\\\r\n") != -1) {
            str = str.replace("\\\r\n", "\n");
        }
        if (str.indexOf("\\\n") != -1) {
            str = str.replace("\\\n", "\n");
        }
        if (str.indexOf("\\") != -1) {
            String head = str.substring(0, str.indexOf("\\"));
            String tail = str.substring(str.indexOf("\\") + 1);
            str = head.concat(tail);
        }
        if (str.length() > 1024) {
            raiseError("String constant too long");
        } else if (str.contains("\0")) {
            raiseError("String contains null character");
        } else {
            setText(str);
        }
	}
	| NEW_LINE { raiseError("Unterminated string constant"); }
	| EOF { raiseError("EOF in string constant"); }

);


LINE_COMMENT
    : '--' .*? (NEW_LINE | EOF) -> skip;
BLOCK_COMMENT: '(*' (BLOCK_COMMENT | .)*?
                (
                    '*)' {skip();}
	                | EOF { raiseError("EOF in comment"); }
	            );
UNMATCH_COMMENT : '(*' | '*)' {raiseError("Unmatched " + getText());};

INVALID_CHARACTER: . {raiseError("Invalid character: " + getText());};
