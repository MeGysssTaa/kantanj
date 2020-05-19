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
import java.util.regex.Pattern;

public class Condition {

    /**
     * The list of all the subconditions and their branches
     * this conditional statement implies
     */
    final List<Branch> branches = new ArrayList<>();

    /**
     * The branch to go with in case no of the base
     * branches was reached ('* : ELSE_BRANCH')
     */
    Branch elseBranch;

    /**
     * Plaintext standing left to this conditional statement
     */
    String pre;

    /**
     * True - the validate() method had been ran somewhen and had thrown no exceptions.
     * False - the validate() method either had never been ran or had thrown an exception.
     *
     * All methods invoked on a non-validated Condition object will
     * throw an IllegalStateException. Validation is required
     */
    private boolean validated;

    /**
     * Hash code of this Condition and all its branches at the
     * moment when it was validated last time. Used to make sure
     * that there were no changes made to the Condition since its
     * last validation, as well as to avoid duplicate validations.
     *
     * Negative value indicates that no validation was ever made
     */
    private int validatedHash = -1;

    /**
     * Makes sure this Condition and all its branches are valid,
     * or throws a SyntaxException otherwise.
     *
     * If this Condition object was already validated somewhen,
     * and no of its components were changed, then validation is
     * skipped (because of being unnecessary).
     */
    void validate() {
        int h = hashCode();

        if ((validated) && (h != 0) && (h == validatedHash))
            // This setup was already validated
            return;

        validated = false;

        if (pre == null) // that's ok
            pre = ""; // modifies hash code of this Condition => invaliates hash at variable h

        if (branches.isEmpty())
            throw new SyntaxException("missing conditional branches");

        if (elseBranch == null)
            throw new SyntaxException("missing else branch");

        if (elseBranch.result == null)
            throw new SyntaxException("missing result at the else branch");

        for (int i = 0; i < branches.size(); i++) {
            Branch branch = branches.get(i);

            if (branch.obj == -1)
                throw new SyntaxException("missing " +
                        "target obj pointer at branch " + i);

            if (branch.op == null)
                throw new SyntaxException("missing " +
                        "comparison operator at branch " + i);

            if (branch.against == null)
                throw new SyntaxException("missing " +
                        "against obj at branch " + i);

            if (branch.result == null)
                throw new SyntaxException("missing result at branch " + i);
        }

        // All fine, this Condition is safe to use
        validated = true;
        validatedHash = hashCode(); // do not reuse variable h for reasons stated above
    }

    /**
     * Makes sure this condition is validated, or
     * throws an IllegalStateException otherwise.
     */
    private void ensureValidated() {
        if (!(validated))
            throw new IllegalStateException("condition is not validated");
    }

    /**
     * Applies this Condition using the given args.
     * Requires this Condition to be validated.
     *
     * @param args arguments that pointers should take the values from.
     * @return one of this Condition's branches' result String.
     */
    public String apply(Object... args) {
        if ((args == null) || (args.length == 0))
            throw new IllegalArgumentException("args must not be empty or null");

        ensureValidated();

        StringBuilder s = new StringBuilder(pre);
        String result = null;

        for (Branch branch : branches) {
            if (branch.test(args)) {
                result = branch.result;
                break;
            }
        }

        if (result == null)
            result = elseBranch.result;
        s.append(result);

        return s.toString();
    }

    /**
     * Calculate the hash code of this Condition.
     */
    @Override
    public int hashCode() {
        if ((elseBranch == null) || (pre == null))
            return 0; // invalid condition

        int h = 31 * (1 + branches.size());

        h += pre.hashCode();
        h += 31 * elseBranch.result.hashCode();

        for (int i = 0; i < branches.size(); i++) {
            int hBranch = branches.get(i).hashCode();

            if (hBranch == 0)
                return 0; // invalid branch
            h += 31 * (i + 1) + 31 * hBranch;
        }

        return Math.abs(h);
    }

    /**
     * Returns the presence of this Condition as a String.
     * Requires this Condition to be validated.
     *
     * @return the presence of this Condition as a String.
     */
    @Override
    public String toString() {
        ensureValidated();

        StringBuilder s = new StringBuilder("Condition: { branches: (");

        if (branches.isEmpty())
            s.append("none");
        else {
            for (int i = 0; i < branches.size(); i++) {
                s.append(branches.get(i).toString());

                if (i < (branches.size() - 1))
                    s.append(", ");
            }
        }

        s.append("); elseBranch: ").append(elseBranch.toString());
        s.append("; pre: \"").append(pre).append("\"}");

        return s.toString();
    }

    static class Branch {
        private static final String RANGE_LIMITS_SPLIT = Pattern.quote("..");

        /**
         * Object this condition is checked at.
         *
         * Must be a pointer: {0}, {1}, ..., {n}
         *                     0    1   ...   n
         */
        int obj = -1;

        /**
         * Comparison operator.
         *
         * Should be one of: "=", ">", ">=", "<", "<="
         */
        String op;

        /**
         * Should this condition operator be negated (reversed).
         * For example, "a!=b" is only true when "a=b" would be false
         */
        boolean negate;

        /**
         * Object the one at the pointer 'obj' is compared against.
         * Can be either a pure object or another pointer
         */
        String against;

        /**
         * The string to return in case this
         * branch's condition returns true
         */
        String result;

        /**
         * Attempts to apply this conditional branch to the given args.
         *
         * @param args the objects this Branch's pointers should take values from.
         *             Assuming that the method can only be invoked by Condition#apply,
         *             args are guaranteed to be non-null and not empty. Moreoever it
         *             is guaranteed that the Condition is validated, and hence this
         *             branch is valid and safe to use.
         *
         * @throws IllegalArgumentException if one or more of this Branch's pointers
         *                                  point to an argument index that the specified
         *                                  args array does not contain.
         *
         * @throws NullPointerException if the object located at the index one of the
         *                              pointers points to is null.
         *
         * @throws SyntaxException if the object to compare against is an invalid pointer;
         *                             ** OR **
         *                         if the operator is set to "~" (range check), but the target
         *                         object is not a number;
         *                             ** OR **
         *                         if the operator is set to "~" (range check), and the low
         *                         range limit is greater than or equal to its high range;
         *                             ** OR **
         *                         if the specified operator is not applicable for the type
         *                         of objects being compared;
         *                             ** OR **
         *                         if the objects that should be compared are of different types.
         *
         * @return true if success, false otherwise.
         *         False indicates that this Branch returns false on the given args.
         */
        private boolean test(Object... args) {
            if (obj >= args.length)
                throw new IllegalArgumentException("too " +
                        "few args: expected value at index " + obj);

            Object target = args[obj];

            if (target == null)
                throw new NullPointerException("object " +
                        "at index " + obj + " must not be null");

            Object compareTo;

            int idxOfOpening = against.indexOf('{');
            int idxOfClosing = against.indexOf('}');

            if (idxOfClosing > idxOfOpening) {
                // The object to compare to is also a pointer ('{i}').
                // This requires some extra validation that the against
                // pointer is valid and safe to use.
                int pointer;
                String pointerStr = against.replace("{", "").
                                            replace("}", "");

                try {
                    pointer = Integer.parseInt(pointerStr);
                } catch (NumberFormatException ex) {
                    throw new SyntaxException("invalid pointer: " + against);
                }

                if (pointer >= args.length)
                    throw new IllegalArgumentException("too " +
                            "few args: expected value at index " + pointer);

                compareTo = args[pointer];

                if (compareTo == null)
                    throw new NullPointerException("object " +
                            "at index " + obj + " must not be null");
            } else {
                if (op.equals("~"))
                    // This is a range check. No against object needed
                    compareTo = null;
                else {
                    // Pickup an appropriate type for the against object
                    if (against.contains(".")) {
                        // Possibly a double
                        try {
                            compareTo = Double.parseDouble(against);
                        } catch (NumberFormatException ex) {
                            // No, just a string with some dots
                            compareTo = against;
                        }
                    } else {
                        // Maybe an int?
                        try {
                            compareTo = Integer.parseInt(against);
                        } catch (NumberFormatException ex) {
                            // No. That's a string
                            compareTo = against;
                        }
                    }
                }
            }

            Class targetClass = target.getClass();
            Class againstClass = (compareTo == null) ? null : compareTo.getClass();

            if ((compareTo != null) && (targetClass != againstClass))
                throw new SyntaxException(String.format("types mismatch:" +
                        " cannot compare %s to %s", targetClass.getSimpleName(), againstClass.getSimpleName()));

            boolean match;

            if (op.equals("~")) {
                // Make sure the target object is a number
                if (target instanceof Number) {
                    // Check if the target number is in the specified range
                    String[] limits = against.split(RANGE_LIMITS_SPLIT);

                    double low = Double.parseDouble(limits[0]);
                    double high = Double.parseDouble(limits[1]);

                    if (low >= high)
                        throw new SyntaxException(String.format("low range limit must " +
                                "be less than high range limit; low: %s, high: %s", low, high));

                    double tNum = ((Number) target).doubleValue();
                    match = tNum >= low && tNum <= high;
                } else throw new SyntaxException("target object must be a number for range checks");
            } else {
                // It is guaranteed by a check above that compareTo
                // is not null if the operator is not equal to "~"
                assert compareTo != null;

                if (target instanceof Number) {
                    // The above class check guarantees that if one of
                    // objects is a Number, the other one is also a Number.
                    double nTarget = ((Number) target).doubleValue();
                    double nAgainst = ((Number) compareTo).doubleValue();
                    
                    match = compareNumbers(nTarget, nAgainst);
                } else {
                    // The only other supported type is string.
                    // The only comparison operator for String we
                    // are going to accept is strict equals ('=')
                    if (op.equals("="))
                        match = target.equals(compareTo);
                    else throw new SyntaxException("operator " + op +
                            " is not applicable for objects of type String");
                }
            }
            
            return negate != match; // negate if needed
        }

        /**
         * Compare the specified numbers using the operator set during parsing.
         *
         * @throws SyntaxException if the comparison operator is not applicable
         *                         for numbers.
         *
         * @return true if the condition nTarget(?)nAgainst is true, where
         *         (?) is the comparison operator used; false otherwise.
         */
        private boolean compareNumbers(double nTarget, double nAgainst) {
            // It should be guaranteed by the checks made during string parsing
            // that the operator is either "~" or one of the following:
            // =, <, <=, >, >=.
            switch (op) {
                case "=":
                    return nTarget == nAgainst;

                case "<":
                    return nTarget < nAgainst;

                case "<=":
                    return nTarget <= nAgainst;

                case ">":
                    return nTarget > nAgainst;

                case ">=":
                    return nTarget >= nAgainst;
            }

            throw new SyntaxException("operator " + op +
                    " is not applicable for objects of type Number");
        }

        /**
         * Calculate the hash code of this Branch.
         */
        @Override
        public int hashCode() {
            if ((obj == -1) || (op == null)
                    || (against == null) || (result == null))
                return 0; // invalid branch

            int h = 31 * obj
                    + ((negate) ? 33 : 31) * op.hashCode()
                    + against.hashCode()
                    + result.hashCode();

            return Math.abs(h);
        }

        /**
         * Returns the presence of this Branch as a String.
         * @return the presence of this Branch as a String.
         */
        @Override
        public String toString() {
            return String.format("Branch[obj: %s, op: %s, negate: %s, against: %s, result: \"%s\"]",
                    obj, op, negate, against, result);
        }
    }

}
