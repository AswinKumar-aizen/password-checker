/*
 * Password Strength Checker (simple OOP project in Java)
 *
 * OOP concepts used:
 *   1. Abstraction   -> Rule is an abstract class: "every rule must be able to check a password"
 *   2. Inheritance   -> LengthRule, UppercaseRule, ... all extend Rule
 *   3. Polymorphism  -> the checker calls rule.evaluate() on every rule without caring which one it is
 *   4. Encapsulation -> fields are private/protected; accessed only through methods
 *   5. Composition   -> PasswordStrengthChecker HAS-A list of Rule objects
 *
 * Total score = 10 points.
 * Compile: javac PasswordStrengthChecker.java
 * Run    : java PasswordStrengthChecker
 * (or in one step: java PasswordStrengthChecker.java)
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// ---------------------------------------------------------------
// Program entry point
// ---------------------------------------------------------------
public class PasswordStrengthChecker {
    public static void main(String[] args) {
        PasswordChecker checker = new PasswordChecker();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Password Strength Checker ===");
        System.out.println("(type 'exit' to quit)");

        while (true) {
            System.out.print("\nEnter password: ");
            if (!scanner.hasNextLine()) break;
            String password = scanner.nextLine();

            if (password.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty.");
                continue;
            }
            checker.check(password).display();
        }
        scanner.close();
    }
}

// ---------------------------------------------------------------
// 1. ABSTRACTION: base class that every rule must follow
// ---------------------------------------------------------------
abstract class Rule {
    private final int points;        // 4. Encapsulation: private data
    private final String tip;        // advice shown if the rule fails
    private final boolean critical;  // if a critical rule fails, password is capped at "Weak"

    protected Rule(int points, String tip, boolean critical) {
        this.points = points;
        this.tip = tip;
        this.critical = critical;
    }

    // Each child class decides how to check its own rule
    protected abstract boolean isPassed(String password);

    public boolean isCritical() {
        return critical;
    }

    // Same for all rules: returns points earned (0 if the rule fails)
    public int evaluate(String password) {
        return isPassed(password) ? points : 0;
    }

    public String getTip() {
        return tip;
    }
}

// ---------------------------------------------------------------
// 2. INHERITANCE: each rule extends Rule and implements isPassed()
// ---------------------------------------------------------------
class LengthRule extends Rule {
    private final int minLength;

    public LengthRule(int minLength, int points, String tip, boolean critical) {
        super(points, tip, critical);
        this.minLength = minLength;
    }

    @Override
    protected boolean isPassed(String password) {
        return password.length() >= minLength;
    }
}

class LowercaseRule extends Rule {
    public LowercaseRule() {
        super(1, "Add at least one lowercase letter (a-z).", false);
    }

    @Override
    protected boolean isPassed(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isLowerCase(ch)) return true;
        }
        return false;
    }
}

class UppercaseRule extends Rule {
    public UppercaseRule() {
        super(1, "Add at least one uppercase letter (A-Z).", false);
    }

    @Override
    protected boolean isPassed(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) return true;
        }
        return false;
    }
}

class DigitRule extends Rule {
    public DigitRule() {
        super(1, "Add at least one number (0-9).", false);
    }

    @Override
    protected boolean isPassed(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isDigit(ch)) return true;
        }
        return false;
    }
}

class SpecialCharRule extends Rule {
    public SpecialCharRule() {
        super(1, "Add at least one special character (e.g. @, #, $, !).", false);
    }

    @Override
    protected boolean isPassed(String password) {
        for (char ch : password.toCharArray()) {
            if (!Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch)) return true;
        }
        return false;
    }
}

class CommonPasswordRule extends Rule {
    private static final List<String> COMMON_WORDS = Arrays.asList(
            "password", "qwerty", "admin", "welcome", "letmein", "iloveyou", "abc123", "123456");

    public CommonPasswordRule() {
        super(2, "Avoid common words like 'password', 'qwerty' or '123456'.", true);
    }

    @Override
    protected boolean isPassed(String password) {
        String lowered = password.toLowerCase();
        for (String word : COMMON_WORDS) {
            if (lowered.contains(word)) return false;
        }
        return true;
    }
}

class RepeatedCharRule extends Rule {
    public RepeatedCharRule() {
        super(1, "Avoid repeating the same character 3 times in a row (e.g. 'aaa').", false);
    }

    @Override
    protected boolean isPassed(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1)
                    && password.charAt(i) == password.charAt(i + 2)) {
                return false;
            }
        }
        return true;
    }
}

// ---------------------------------------------------------------
// Result object: just holds the output (keeps code clean)
// ---------------------------------------------------------------
class StrengthResult {
    private final int score;
    private final String label;
    private final List<String> suggestions;

    public StrengthResult(int score, String label, List<String> suggestions) {
        this.score = score;
        this.label = label;
        this.suggestions = suggestions;
    }

    public int getScore() { return score; }

    public String getLabel() { return label; }

    public List<String> getSuggestions() { return suggestions; }

    public void display() {
        String bar = "#".repeat(score) + "-".repeat(10 - score);
        System.out.println("\nScore    : " + score + "/10  [" + bar + "]");
        System.out.println("Strength : " + label);

        if (suggestions.isEmpty()) {
            System.out.println("\nGreat job! No improvements needed.");
        } else {
            System.out.println("\nHow to improve:");
            for (String tip : suggestions) {
                System.out.println("  - " + tip);
            }
        }
    }
}

// ---------------------------------------------------------------
// 5. COMPOSITION: the checker HAS a list of rules
// ---------------------------------------------------------------
class PasswordChecker {
    private final List<Rule> rules = new ArrayList<>();

    public PasswordChecker() {
        rules.add(new LengthRule(8, 2, "Use at least 8 characters.", true));
        rules.add(new LengthRule(12, 1, "Use 12 or more characters for extra security.", false));
        rules.add(new LowercaseRule());
        rules.add(new UppercaseRule());
        rules.add(new DigitRule());
        rules.add(new SpecialCharRule());
        rules.add(new CommonPasswordRule());
        rules.add(new RepeatedCharRule());
    }

    public StrengthResult check(String password) {
        int score = 0;
        boolean capped = false;
        List<String> suggestions = new ArrayList<>();

        // 3. POLYMORPHISM: same call, different behaviour for each rule
        for (Rule rule : rules) {
            int earned = rule.evaluate(password);
            score += earned;

            if (earned == 0) {                       // rule failed
                suggestions.add(rule.getTip());
                if (rule.isCritical()) {
                    capped = true;                   // basic rule failed -> can't be better than Weak
                }
            }
        }

        if (capped) {
            score = Math.min(score, 3);
        }
        return new StrengthResult(score, getLabel(score), suggestions);
    }

    private String getLabel(int score) {
        if (score <= 3) return "Weak";
        if (score <= 5) return "Medium";
        if (score <= 8) return "Strong";
        return "Very Strong";
    }
}
