/*
 * Copyright 2020 DarksideCode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.darksidecode.kantanj.formatting;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import java.util.ArrayList;
import java.util.List;

public class ConditionalFormatter {

    /**
     * Allowed comparison operators:
     *     '~' - in range. "X~2..5" = "is X in range from 2 to 5 (inclusive)?";
     *     '<' - less than. "X<5" = "is X less than 5?";
     *     '>' - greater than. "X>5" = "is X greater than 5?";
     *     '=' - equals. "X=5" = "is X equal to 5?".
     *
     * NOTE: the '<' and '>' operators may also be followed by an
     * equals ('=') operator in order to make the condition non-strict
     * (for example, "X>=5" = "is X greater than OR equal to 5?").
     */
    private static final String OPS = "~<=>";

    /**
     * Parse state.
     *
     * Expecting either:
     *     (a) a pointer to the object that should be checked; or
     *     (b) an ELSE branch mark ('*').
     */
    private static final int EXPECTING_OBJ       = 0;

    /**
     * Parse state.
     *
     * Expecting a comparison operator (one of OPS) followed
     * by an object or a pointer to an object to compare to.
     */
    private static final int EXPECTING_CONDITION = 1;

    /**
     * Parse state.
     *
     * Expecting an expression (result) that should be
     * displayed in the formatted string in case this
     * branch is reached and is true, followed by a closing
     * quote mark ('\'') to indicate end of a condition if
     * the current branch is an ELSE branch.
     */
    private static final int EXPECTING_RESULT    = 2;

    /**
     * Range spec parse state.
     *
     * Expecting range start number.
     */
    private static final int EXPECT_RANGE_START = 0;

    /**
     * Range spec parse state.
     *
     * Expecting one more dot ('.') char.
     */
    private static final int EXPECT_ONE_DOT     = 1;

    /**
     * Range spec parse state.
     *
     * Expecting range end number.
     */
    private static final int EXPECT_RANGE_END   = 2;

    /**
     * The list of conditional statements the input string contains.
     * May be empty: in that case, no conditional formatting will be performed.
     */
    private final List<Condition> conditions = new ArrayList<>();

    /**
     * Contents of the input string that belong to no conditional
     * statements (the last chars, "the tail", of the string which
     * are located behind the last conditional statement).
     *
     * If the input string contains no conditional statements,
     * then plainTail holds the whole input string itself,
     * assuming that no conditional formatting will be performed.
     */
    private String plainTail = "";

    /**
     * Indicates whether the last registered quote
     * ('\'') is opening and not closing.
     */
    private boolean openingQuote;

    /**
     * Holds index of the last registered quote ('\'') character.
     */
    private int lastQuotePos;

    /**
     * General parsing state (what kind of
     * tokens should the parser expect next).
     */
    private int state = EXPECTING_OBJ;

    /**
     * Range spec parsing state (what kind of tokens
     * should the range spec parser expect next).
     */
    private int rangeSpecState = EXPECT_RANGE_START;

    /**
     * Used to store digits while parsing an obj pointer.
     * Reset after successful state switch.
     */
    private StringBuilder curNum = new StringBuilder();

    /**
     * Used to store OPS characters while parsing an operator.
     * Reset after successful state switch.
     */
    private StringBuilder op = new StringBuilder();

    /**
     * Used to store symbols or pointers while parsing an against object.
     * Reset after successful state switch.
     */
    private StringBuilder against = new StringBuilder();

    /**
     * Used to store symbols or pointers while parsing a result.
     * Reset after successful state switch (condition end).
     */
    private StringBuilder result = new StringBuilder();

    /**
     * Holds the conditional branch which is being parsed at the moment.
     */
    private Condition.Branch curBranch = new Condition.Branch();

    /**
     * Holds the condition which is being parsed at the moment.
     */
    private Condition curCond;

    /**
     * Indicates whether the curNum should be
     * appended by the subsequent chars.
     */
    private boolean appendCurNum;

    /**
     * Indicates whether the branch that is currently
     * being parsed is the ELSE branch.
     */
    private boolean elseBranch;

    /**
     * Indicates that we are currently parsing a range spec.
     */
    private boolean rangeSpec;

    /**
     * Construct a new ConditionalFormatter based on the given String.
     *
     * If the given string is not null and is at least 1 character long
     * (even trimmed), then it is parsed fully, making this formatter
     * ready for subsequent actions such as string transformation
     * (conditional and ordinary formatting and other). Otherwise an
     * IllegalArgumentException is thrown to indicate that the input
     * string is invalid.
     *
     * @param s string to parse.
     *
     * @throws IllegalArgumentException if the specified string is either
     *                                  null, empty or contains only spaces.
     *
     * @throws SyntaxException if the specified string cannot be parsed
     *                         by this formatter properly.
     */
    public ConditionalFormatter(String s) {
        if ((s == null) || (s.trim().isEmpty()))
            throw new IllegalArgumentException("input string cannot be empty");

        char[] chars = s.toCharArray();
        StringBuilder pre = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '\'') {
                openingQuote = !openingQuote;
                lastQuotePos = i;

                if (openingQuote) {
                    if (i == (chars.length - 1))
                        throw new SyntaxException("unclosed quote at char " + (i + 1));

                    // parseCondition tells the loop to skip some
                    // subsequent chars which it has already parsed
                    i = parseCondition(chars, i, pre.toString());
                    pre = new StringBuilder();
                }
                // closing quote is handled inside the parseCondition method
            } else pre.append(c);
        }

        if (openingQuote)
            throw new SyntaxException("unclosed quote at char " + (lastQuotePos + 1));

        if (pre.length() > 0)
            // Free chars that were not bound to any conditions.
            // Should be put in the end of formatted strings
            plainTail = pre.toString();

        // Validate all conditions
        conditions.forEach(Condition::validate);
    }

    /**
     * Parse one Condition.
     *
     * @param chars array to look for tokens in.
     * @param start condition start position.
     * @param pre condition prefix.
     *
     * @throws SyntaxException if this condition's syntax is invalid
     *                         and cannot be parsed.
     *
     * @return the number of characters to skip after parse.
     */
    private int parseCondition(char[] chars, int start, String pre) {
        char[] innerChars = getInner(chars, start);

        curCond = new Condition();
        curCond.pre = pre;

        for (int pos = 0; pos < innerChars.length; pos++) {
            char ic = innerChars[pos];

            boolean skipSpace = ic == ' ' && state != EXPECTING_RESULT;
            boolean skipColon = ic == ':' && elseBranch;

            if ((skipSpace) || (skipColon))
                continue;

            switch (state) {
                case EXPECTING_OBJ:
                    parseExpectingObj(ic, start, pos);
                    break;

                case EXPECTING_CONDITION:
                    parseExpectingCondition(ic, start, pos);
                    break;

                case EXPECTING_RESULT:
                    int skip = parseExpectingResult(ic, start, pos);

                    if (skip != -1)
                        // reached end of condition
                        return skip;

                    break;

                default:
                    throw new IllegalStateException(Integer.toString(state));
            }
        }

        throw new SyntaxException("unterminated condition at char " + (start + 1));
    }

    /**
     * Parse state: expecting obj.
     * Look for and parse a pointer to the target conditional object.
     *
     * @param ic current token.
     * @param start condition start position.
     * @param pos position of the current token.
     *
     * @throws SyntaxException if this conditional branch's syntax
     *                         is invalid and cannot be parsed.
     */
    private void parseExpectingObj(char ic, int start, int pos) {
        if (ic == '*') {
            elseBranch = true;
            state = EXPECTING_RESULT;
        } else if (ic == '{')
            appendCurNum = true;
        else if (appendCurNum) {
            if (ic == '}') {
                if (curNum.length() == 0)
                    throw new SyntaxException("empty pointer" +
                            " at char " + (start + 1 + pos + 1));

                curBranch.obj = Integer.parseInt(curNum.toString());
                curNum = new StringBuilder();

                appendCurNum = false;
                state = EXPECTING_CONDITION;
            } else {
                if (!(Character.isDigit(ic)))
                    throw new SyntaxException("invalid non-int " +
                            "pointer at char " + (start + 1 + pos + 1));

                curNum.append(ic);
            }
        }
    }

    /**
     * Parse state: expecting condition.
     * Look for and parse a conditional operator and
     * the object the target should be compared against.
     *
     * @param ic current token.
     * @param start condition start position.
     * @param pos position of the current token.
     *
     * @throws SyntaxException if this conditional branch's syntax
     *                         is invalid and cannot be parsed.
     */
    private void parseExpectingCondition(char ic, int start, int pos) {
        boolean operatorChar = OPS.indexOf(ic) != -1;

        if (!(rangeSpec))
            rangeSpec = op.length() == 1 && op.charAt(0) == '~';

        if (ic == '!')
            curBranch.negate = true;
        else if ((operatorChar) || (rangeSpec)) {
            boolean prevCharDigit = op.length() > 1
                    && Character.isDigit(op.charAt(op.length() - 1));

            op.append(ic);

            if (rangeSpec) {
                // Must be either a digit or a dot (".") for range
                // spec or a colon (":") for range spec end
                boolean validChar = ic == '.' || ic == ':' || Character.isDigit(ic);

                if (!(validChar))
                    throw new SyntaxException(String.format("illegal " +
                            "symbol %s in range spec at char %s", ic, start + 1 + pos + 1));

                if ((rangeSpecState == EXPECT_RANGE_START) && (ic == '.')) {
                    if (prevCharDigit)
                        rangeSpecState = EXPECT_ONE_DOT; // range start found; expect one more dot (".")
                    else throw new SyntaxException("expected range " +
                            "start number at char " + (start + 1 + pos + 1));
                } else if (rangeSpecState == EXPECT_ONE_DOT) {
                    if (ic == '.')
                        rangeSpecState = EXPECT_RANGE_END;
                    else throw new SyntaxException("expected range " +
                            "start/end separator \"..\" at char " + (start + 1 + pos + 1));
                } else if (rangeSpecState == EXPECT_RANGE_END) {
                    // It is guaranteed by a check above that this is either
                    // a digit, a dot (".") or a colon (":"). At the end of a
                    // range spec we expect nothing but a colon identifying
                    // start of the expression (result) itself
                    if (ic == '.')
                        throw new SyntaxException("expected colon \":\" " +
                                "at the end of range spec at char " + (start + 1 + pos + 1));

                    // Now it is guaranteed that the char
                    // is either a digit or a colon (":")
                    if (ic == ':') {
                        if (prevCharDigit) {
                            // Range end found; expect expression (result) next
                            curBranch.op = "~";
                            curBranch.against = op.substring(1, op.length() - 1); // 'compare' against range

                            rangeSpecState = EXPECT_RANGE_START;
                            rangeSpec = false;
                            op = new StringBuilder();
                            against = new StringBuilder();

                            state = EXPECTING_RESULT;
                        } else throw new SyntaxException("expected range " +
                                "end number at char " + (start + 1 + pos + 1));
                    }
                }
            }
        } else {
            // Check whether we need to set the op...
            if (curBranch.op == null) {
                if (op.length() == 0)
                    throw new SyntaxException("empty " +
                            "comparison operator at char " + (start + 1 + pos + 1));

                curBranch.op = op.toString();
                op = new StringBuilder();
            }

            // ...and if not, then we need to set the against obj
            if (ic == ':') {
                if (against.length() == 0)
                    throw new SyntaxException("nothing to " +
                            "compare to at char " + (start + 1 + pos + 1));

                curBranch.against = against.toString();
                against = new StringBuilder();

                state = EXPECTING_RESULT;
            } else against.append(ic);
        }
    }

    /**
     * Parse state: expecting result.
     * Look for and parse the branch expression (result).
     *
     * @param ic current token.
     * @param start condition start position.
     * @param pos position of the current token.
     *
     * @throws SyntaxException if this conditional branch's syntax
     *                         is invalid and cannot be parsed.
     */
    private int parseExpectingResult(char ic, int start, int pos) {
        if (ic == '|') {
            if (elseBranch)
                throw new SyntaxException("else branch must be the last branch of " +
                        "a condition: unexpected OR operator at char " + (start + 1 + pos + 1));

            String r = result.toString().trim();

            if (r.isEmpty())
                throw new SyntaxException("expected " +
                        "result at char " + (start + 1 + pos + 1));

            curBranch.result = r;
            curCond.branches.add(curBranch);

            result = new StringBuilder();
            curBranch = new Condition.Branch();

            state = EXPECTING_OBJ; // expect another branch
        } else if (ic == '\'') {
            // End of condition
            if (!(elseBranch))
                throw new SyntaxException("illegal condition end: " +
                        "missing else branch at char " + (start + 1 + pos + 1));

            String r = result.toString().trim();

            if (r.isEmpty())
                throw new SyntaxException("expected " +
                        "result at char " + (start + 1 + pos + 1));

            // register closing quote
            openingQuote = !openingQuote;
            lastQuotePos = start;

            curBranch.result = r;
            curCond.elseBranch = curBranch;
            conditions.add(curCond);

            return start + pos + 1; // skip next (start+pos) chars
        } else result.append(ic);

        return -1;
    }

    /**
     * Returns a sub-array [start..len], where len is
     * the length of the specified character array.
     *
     * @param chars base array.
     * @param start sub-array start index.
     *
     * @return a sub-array with first element equal to
     *         the element of the specified base array
     *         at position (start+1), and all the subsequent
     *         elements equal to the corresponding elements
     *         of the specified base array, with length of
     *         (len-start-1), where len is the length of the
     *         specified base array.
     */
    private char[] getInner(char[] chars, int start) {
        StringBuilder inner = new StringBuilder();

        for (int j = start + 1; j < chars.length; j++) {
            char next = chars[j];
            inner.append(next);

            if (next == '\'') {
                if (j == (start + 1))
                    throw new SyntaxException("empty " +
                            "condition at char " + (start + 1));

                break; // stop appending inner: reached end of condition
            }
        }

        return inner.toString().toCharArray();
    }

    /**
     * Format the input string given to the constructor based
     * on the specified args.
     *
     * If the input string contains nothing to be formatted conditionally
     * (no conditional statements), then simply no conditional formatting
     * is performed. Otherwise it is required that the specified args array
     * meets all these requirements:
     *     (1) it is not null;
     *     (2) it is not empty;
     *     (3) its elements can fulfill requirements of each condition,
     *         i.e. the array must contain all the indexes a condition
     *         points to (for instance, if a condition has a '{3}' pointer,
     *         then the array must be at least 4 objects long so that there
     *         is something at index 3);
     *     (4) no of its elements conditions point to are null.
     *
     * Regardless of whether conditional formatting has been performed
     * or not, ordinary formatting is then made. It replaces all substrings
     * of form '{n}' with objects of the given array at index n, where n can
     * be any positive integer. Array element at index n must not be null.
     * For ordinary formatting, the specified array is allowed to be empty.
     * In that case, no ordinary formatting is performed.
     *
     * @param args array conditional and ordinary pointers select objects from.
     *
     * @return input string, with all substrings in conditional ('O ? A : R | * : E')
     *         format replaced with non-null objects from the specified array based on conditions'
     *         results, and all strings in pointing ({i}) format replaced with non-null objects
     *         from the specified array.
     */
    public String format(Object... args) {
        if (args == null)
            throw new NullPointerException("args cannot be null");

        // Perform conditional formatting (if any)
        StringBuilder formatted = new StringBuilder();
        conditions.forEach(cond -> formatted.append(cond.apply(args)));

        // Perform ordinary formatting
        String f = formatted.toString() + plainTail;

        for (int i = 0; i < args.length; i++) {
            Object o = args[i];

            if (o == null)
                throw new NullPointerException("object " +
                        "at index " + i + " must not be null");

            f = f.replace("{" + i + "}", o.toString());
        }

        return f;
    }

}
