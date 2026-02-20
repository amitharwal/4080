private Expr recoverMissingLeftOperand(Token operator) {
    error(operator, "Missing left-hand operand.");

    // Parse a RHS at the operator’s precedence level (or the next tighter level).
    // Returning the RHS lets the rest of the parse proceed.
    switch (operator.type) {
        case OR:
            return and();

        case AND:
            return equality();

        case BANG_EQUAL:
        case EQUAL_EQUAL:
            return comparison();

        case GREATER:
        case GREATER_EQUAL:
        case LESS:
        case LESS_EQUAL:
            return term();

        case PLUS:
        case MINUS:
            return factor();

        case STAR:
        case SLASH:
            return unary();

        default:
            // Fallback: consume something expression-like.
            return unary();
    }
}