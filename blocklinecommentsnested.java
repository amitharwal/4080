public class blocklinecommentsnested {




    int depth = 1;

    while (depth > 0 && !isAtEnd()) {
            if (peek() == '/' && peekNext() == '*') {
                advance();
                advance();
                depth++;
            } else if (peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                depth--;
            } else {
                if (peek() == '\n') line++;
                advance();
            }
        }

    if (depth > 0) {
            error(line, "Unterminated block comment.");
        }




}
