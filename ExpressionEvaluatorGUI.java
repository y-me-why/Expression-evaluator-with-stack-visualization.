import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


/**
 * Expression Evaluator with Visual Stack Representation
 * Supports Infix, Postfix, and Prefix expression evaluation
 * Uses Java Swing for GUI and visual stack animation
 */
public class ExpressionEvaluatorGUI extends JFrame implements ActionListener {
    // GUI Components
    private JTextField inputField;
    private JTextArea resultArea;
    private JRadioButton infixRadio, postfixRadio, prefixRadio;
    private ButtonGroup expressionTypeGroup;
    private JButton evaluateButton, convertButton, stepButton, resetButton;
    private VisualStack visualStack;
    private JPanel controlPanel;
    private JTextArea stepArea;
    private ExpressionParser parser;
    
    // Animation control
    private javax.swing.Timer animationTimer;  // Explicitly use javax.swing.Timer
    private List<StackOperation> operationQueue;
    private int currentOperationIndex;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Fixed: Use getSystemLookAndFeelClassName() instead of getSystemLookAndFeel()
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ExpressionEvaluatorGUI().setVisible(true);
        });
    }
    
    public ExpressionEvaluatorGUI() {
        parser = new ExpressionParser();
        operationQueue = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Expression Evaluator with Visual Stack");
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private void initializeComponents() {
        // Input components
        inputField = new JTextField(20);
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        
        // Expression type selection
        infixRadio = new JRadioButton("Infix", true);
        postfixRadio = new JRadioButton("Postfix");
        prefixRadio = new JRadioButton("Prefix");
        expressionTypeGroup = new ButtonGroup();
        expressionTypeGroup.add(infixRadio);
        expressionTypeGroup.add(postfixRadio);
        expressionTypeGroup.add(prefixRadio);
        
        // Buttons
        evaluateButton = new JButton("Evaluate");
        convertButton = new JButton("Convert");
        stepButton = new JButton("Step Through");
        resetButton = new JButton("Reset");
        
        // Result display
        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setBackground(new Color(240, 240, 240));
        
        // Step-by-step display
        stepArea = new JTextArea(8, 30);
        stepArea.setEditable(false);
        stepArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        stepArea.setBackground(new Color(250, 250, 250));
        
        // Visual stack
        visualStack = new VisualStack();
        
        // Animation timer - Fixed: Use javax.swing.Timer
        animationTimer = new javax.swing.Timer(1000, e -> performNextOperation());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel - Input and controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Expression:"));
        inputPanel.add(inputField);
        
        JPanel radioPanel = new JPanel(new FlowLayout());
        radioPanel.add(infixRadio);
        radioPanel.add(postfixRadio);
        radioPanel.add(prefixRadio);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(evaluateButton);
        buttonPanel.add(convertButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(resetButton);
        
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(radioPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Center panel - Visual stack and results
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Left side - Visual stack
        JPanel stackPanel = new JPanel(new BorderLayout());
        stackPanel.setBorder(BorderFactory.createTitledBorder("Visual Stack"));
        stackPanel.add(visualStack, BorderLayout.CENTER);
        
        // Right side - Results and steps
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        JPanel stepPanel = new JPanel(new BorderLayout());
        stepPanel.setBorder(BorderFactory.createTitledBorder("Step-by-Step"));
        stepPanel.add(new JScrollPane(stepArea), BorderLayout.CENTER);
        
        rightPanel.add(resultPanel, BorderLayout.NORTH);
        rightPanel.add(stepPanel, BorderLayout.CENTER);
        
        centerPanel.add(stackPanel, BorderLayout.WEST);
        centerPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        
        // Add operator precedence reference
        add(createReferencePanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createReferencePanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Operator Precedence"));
        JLabel precedenceLabel = new JLabel(
            "Precedence (High to Low): () > ^ > *, /, % > +, - | Associativity: ^ (Right), Others (Left)"
        );
        precedenceLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(precedenceLabel);
        return panel;
    }
    
    private void setupEventListeners() {
        evaluateButton.addActionListener(this);
        convertButton.addActionListener(this);
        stepButton.addActionListener(this);
        resetButton.addActionListener(this);
        
        inputField.addActionListener(e -> evaluateExpression());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Evaluate":
                evaluateExpression();
                break;
            case "Convert":
                convertExpression();
                break;
            case "Step Through":
                stepThroughExpression();
                break;
            case "Reset":
                resetAll();
                break;
        }
    }
    
    private void evaluateExpression() {
        String expression = inputField.getText().trim();
        if (expression.isEmpty()) {
            showError("Please enter an expression");
            return;
        }
        
        try {
            String result = "";
            if (infixRadio.isSelected()) {
                double value = parser.evaluateInfix(expression);
                result = "Result: " + value + "\n";
                result += "Postfix: " + parser.infixToPostfix(expression) + "\n";
                result += "Prefix: " + parser.infixToPrefix(expression);
            } else if (postfixRadio.isSelected()) {
                double value = parser.evaluatePostfix(expression);
                result = "Result: " + value;
            } else if (prefixRadio.isSelected()) {
                double value = parser.evaluatePrefix(expression);
                result = "Result: " + value;
            }
            
            resultArea.setText(result);
            visualStack.clear();
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }
    
    private void convertExpression() {
        String expression = inputField.getText().trim();
        if (expression.isEmpty()) {
            showError("Please enter an expression");
            return;
        }
        
        try {
            String result = "";
            if (infixRadio.isSelected()) {
                result = "Original Infix: " + expression + "\n";
                result += "Postfix: " + parser.infixToPostfix(expression) + "\n";
                result += "Prefix: " + parser.infixToPrefix(expression);
            } else {
                result = "Conversion only available for Infix expressions";
            }
            
            resultArea.setText(result);
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }
    
    private void stepThroughExpression() {
        String expression = inputField.getText().trim();
        if (expression.isEmpty()) {
            showError("Please enter an expression");
            return;
        }
        
        try {
            operationQueue.clear();
            currentOperationIndex = 0;
            
            if (infixRadio.isSelected()) {
                operationQueue = parser.getInfixToPostfixSteps(expression);
            } else if (postfixRadio.isSelected()) {
                operationQueue = parser.getPostfixEvaluationSteps(expression);
            } else if (prefixRadio.isSelected()) {
                operationQueue = parser.getPrefixEvaluationSteps(expression);
            }
            
            visualStack.clear();
            stepArea.setText("Step-by-step execution:\n");
            animationTimer.start();
        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
        }
    }
    
    private void performNextOperation() {
        if (currentOperationIndex < operationQueue.size()) {
            StackOperation operation = operationQueue.get(currentOperationIndex);
            
            // Update visual stack
            if (operation.getType() == StackOperation.Type.PUSH) {
                visualStack.push(operation.getValue());
            } else if (operation.getType() == StackOperation.Type.POP) {
                visualStack.pop();
            }
            
            // Update step area
            stepArea.append("Step " + (currentOperationIndex + 1) + ": " + operation.getDescription() + "\n");
            stepArea.setCaretPosition(stepArea.getDocument().getLength());
            
            currentOperationIndex++;
        } else {
            animationTimer.stop();
            stepArea.append("\nExecution completed!");
        }
    }
    
    private void resetAll() {
        inputField.setText("");
        resultArea.setText("");
        stepArea.setText("");
        visualStack.clear();
        animationTimer.stop();
        operationQueue.clear();
        currentOperationIndex = 0;
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}


/**
 * Visual representation of stack operations
 */
class VisualStack extends JPanel {
    private Stack<String> stack;
    private static final int ELEMENT_HEIGHT = 30;
    private static final int ELEMENT_WIDTH = 80;
    private static final Color STACK_COLOR = new Color(135, 206, 250);
    private static final Color BORDER_COLOR = Color.DARK_GRAY;
    private static final Color TEXT_COLOR = Color.BLACK;
    
    public VisualStack() {
        stack = new Stack<>();
        setPreferredSize(new Dimension(120, 400));
        setBackground(Color.WHITE);
    }
    
    public void push(String value) {
        stack.push(value);
        repaint();
    }
    
    public String pop() {
        if (!stack.isEmpty()) {
            String value = stack.pop();
            repaint();
            return value;
        }
        return null;
    }
    
    public void clear() {
        stack.clear();
        repaint();
    }
    
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    public String peek() {
        return stack.isEmpty() ? null : stack.peek();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = (getWidth() - ELEMENT_WIDTH) / 2;
        int y = getHeight() - 20;
        
        // Draw base
        g2d.setColor(BORDER_COLOR);
        g2d.fillRect(x - 5, y, ELEMENT_WIDTH + 10, 5);
        
        // Draw stack elements from bottom to top
        for (int i = 0; i < stack.size(); i++) {
            String element = stack.get(i);
            int elementY = y - (i + 1) * ELEMENT_HEIGHT;
            
            // Draw element rectangle
            g2d.setColor(STACK_COLOR);
            g2d.fillRect(x, elementY, ELEMENT_WIDTH, ELEMENT_HEIGHT);
            
            // Draw border
            g2d.setColor(BORDER_COLOR);
            g2d.drawRect(x, elementY, ELEMENT_WIDTH, ELEMENT_HEIGHT);
            
            // Draw text
            g2d.setColor(TEXT_COLOR);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (ELEMENT_WIDTH - fm.stringWidth(element)) / 2;
            int textY = elementY + (ELEMENT_HEIGHT + fm.getAscent()) / 2;
            g2d.drawString(element, textX, textY);
            
            // Highlight top element
            if (i == stack.size() - 1) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x - 1, elementY - 1, ELEMENT_WIDTH + 2, ELEMENT_HEIGHT + 2);
            }
        }
        
        // Draw stack label
        g2d.setColor(BORDER_COLOR);
        g2d.drawString("STACK", x + 10, 15);
        
        // Draw stack size
        g2d.drawString("Size: " + stack.size(), x + 10, y + 20);
        
        g2d.dispose();
    }
}


/**
 * Expression parser with stack-based algorithms
 */
class ExpressionParser {
    
    public double evaluateInfix(String expression) {
        String postfix = infixToPostfix(expression);
        return evaluatePostfix(postfix);
    }
    
    public double evaluatePostfix(String expression) {
        Stack<Double> stack = new Stack<>();
        String[] tokens = expression.split("\\s+");
        
        for (String token : tokens) {
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid postfix expression");
                }
                double b = stack.pop();
                double a = stack.pop();
                double result = performOperation(token, a, b);
                stack.push(result);
            } else {
                try {
                    stack.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }
        
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix expression");
        }
        
        return stack.pop();
    }
    
    public double evaluatePrefix(String expression) {
        Stack<Double> stack = new Stack<>();
        String[] tokens = expression.split("\\s+");
        
        // Process tokens from right to left
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid prefix expression");
                }
                double a = stack.pop();
                double b = stack.pop();
                double result = performOperation(token, a, b);
                stack.push(result);
            } else {
                try {
                    stack.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }
        
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid prefix expression");
        }
        
        return stack.pop();
    }
    
    public String infixToPostfix(String expression) {
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isWhitespace(c)) {
                continue;
            }
            
            if (Character.isDigit(c) || Character.isLetter(c)) {
                result.append(c);
                // Handle multi-digit numbers
                while (i + 1 < expression.length() && 
                       (Character.isDigit(expression.charAt(i + 1)) || 
                        expression.charAt(i + 1) == '.')) {
                    result.append(expression.charAt(++i));
                }
                result.append(" ");
            } else if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    result.append(stack.pop()).append(" ");
                }
                if (!stack.isEmpty()) {
                    stack.pop(); // Remove '('
                }
            } else if (isOperator(String.valueOf(c))) {
                while (!stack.isEmpty() && 
                       stack.peek() != '(' && 
                       hasHigherOrEqualPrecedence(stack.peek(), c)) {
                    result.append(stack.pop()).append(" ");
                }
                stack.push(c);
            }
        }
        
        while (!stack.isEmpty()) {
            result.append(stack.pop()).append(" ");
        }
        
        return result.toString().trim();
    }
    
    public String infixToPrefix(String expression) {
        // Reverse the expression
        String reversed = new StringBuilder(expression).reverse().toString();
        
        // Replace ( with ) and vice versa
        reversed = reversed.replace('(', 'X').replace(')', '(').replace('X', ')');
        
        // Get postfix of reversed expression
        String postfix = infixToPostfix(reversed);
        
        // Reverse the postfix to get prefix
        String[] tokens = postfix.split("\\s+");
        StringBuilder prefix = new StringBuilder();
        for (int i = tokens.length - 1; i >= 0; i--) {
            prefix.append(tokens[i]).append(" ");
        }
        
        return prefix.toString().trim();
    }
    
    public List<StackOperation> getInfixToPostfixSteps(String expression) {
        List<StackOperation> operations = new ArrayList<>();
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isWhitespace(c)) {
                continue;
            }
            
            if (Character.isDigit(c) || Character.isLetter(c)) {
                StringBuilder number = new StringBuilder();
                number.append(c);
                while (i + 1 < expression.length() && 
                       (Character.isDigit(expression.charAt(i + 1)) || 
                        expression.charAt(i + 1) == '.')) {
                    number.append(expression.charAt(++i));
                }
                result.append(number).append(" ");
                operations.add(new StackOperation(StackOperation.Type.OUTPUT, 
                    number.toString(), "Output operand: " + number));
                
            } else if (c == '(') {
                stack.push(c);
                operations.add(new StackOperation(StackOperation.Type.PUSH, 
                    String.valueOf(c), "Push left parenthesis"));
                
            } else if (c == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    char op = stack.pop();
                    result.append(op).append(" ");
                    operations.add(new StackOperation(StackOperation.Type.POP, 
                        String.valueOf(op), "Pop operator: " + op));
                }
                if (!stack.isEmpty()) {
                    stack.pop(); // Remove '('
                    operations.add(new StackOperation(StackOperation.Type.POP, 
                        "(", "Pop left parenthesis"));
                }
                
            } else if (isOperator(String.valueOf(c))) {
                while (!stack.isEmpty() && 
                       stack.peek() != '(' && 
                       hasHigherOrEqualPrecedence(stack.peek(), c)) {
                    char op = stack.pop();
                    result.append(op).append(" ");
                    operations.add(new StackOperation(StackOperation.Type.POP, 
                        String.valueOf(op), "Pop higher precedence operator: " + op));
                }
                stack.push(c);
                operations.add(new StackOperation(StackOperation.Type.PUSH, 
                    String.valueOf(c), "Push operator: " + c));
            }
        }
        
        while (!stack.isEmpty()) {
            char op = stack.pop();
            result.append(op).append(" ");
            operations.add(new StackOperation(StackOperation.Type.POP, 
                String.valueOf(op), "Pop remaining operator: " + op));
        }
        
        return operations;
    }
    
    public List<StackOperation> getPostfixEvaluationSteps(String expression) {
        List<StackOperation> operations = new ArrayList<>();
        Stack<Double> stack = new Stack<>();
        String[] tokens = expression.split("\\s+");
        
        for (String token : tokens) {
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid postfix expression");
                }
                double b = stack.pop();
                operations.add(new StackOperation(StackOperation.Type.POP, 
                    String.valueOf(b), "Pop operand: " + b));
                
                double a = stack.pop();
                operations.add(new StackOperation(StackOperation.Type.POP, 
                    String.valueOf(a), "Pop operand: " + a));
                
                double result = performOperation(token, a, b);
                stack.push(result);
                operations.add(new StackOperation(StackOperation.Type.PUSH, 
                    String.valueOf(result), "Push result: " + a + " " + token + " " + b + " = " + result));
                
            } else {
                try {
                    double value = Double.parseDouble(token);
                    stack.push(value);
                    operations.add(new StackOperation(StackOperation.Type.PUSH, 
                        token, "Push operand: " + token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }
        
        return operations;
    }
    
    public List<StackOperation> getPrefixEvaluationSteps(String expression) {
        List<StackOperation> operations = new ArrayList<>();
        Stack<Double> stack = new Stack<>();
        String[] tokens = expression.split("\\s+");
        
        // Process tokens from right to left
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid prefix expression");
                }
                double a = stack.pop();
                operations.add(new StackOperation(StackOperation.Type.POP, 
                    String.valueOf(a), "Pop operand: " + a));
                
                double b = stack.pop();
                operations.add(new StackOperation(StackOperation.Type.POP, 
                    String.valueOf(b), "Pop operand: " + b));
                
                double result = performOperation(token, a, b);
                stack.push(result);
                operations.add(new StackOperation(StackOperation.Type.PUSH, 
                    String.valueOf(result), "Push result: " + a + " " + token + " " + b + " = " + result));
                
            } else {
                try {
                    double value = Double.parseDouble(token);
                    stack.push(value);
                    operations.add(new StackOperation(StackOperation.Type.PUSH, 
                        token, "Push operand: " + token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }
        
        return operations;
    }
    
    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || 
               token.equals("/") || token.equals("^") || token.equals("%");
    }
    
    private int getPrecedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
            case '%':
                return 2;
            case '^':
                return 3;
            default:
                return 0;
        }
    }
    
    private boolean hasHigherOrEqualPrecedence(char op1, char op2) {
        int prec1 = getPrecedence(op1);
        int prec2 = getPrecedence(op2);
        
        if (op2 == '^') {
            return prec1 > prec2; // ^ is right associative
        }
        return prec1 >= prec2;
    }
    
    private double performOperation(String operator, double a, double b) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            case "%":
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a % b;
            case "^":
                return Math.pow(a, b);
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}


/**
 * Represents a single stack operation for animation
 */
class StackOperation {
    public enum Type {
        PUSH, POP, OUTPUT
    }
    
    private Type type;
    private String value;
    private String description;
    
    public StackOperation(Type type, String value, String description) {
        this.type = type;
        this.value = value;
        this.description = description;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
}