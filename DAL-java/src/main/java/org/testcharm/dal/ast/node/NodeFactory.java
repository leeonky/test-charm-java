package org.testcharm.dal.ast.node;

import org.testcharm.dal.compiler.GeneratedForJacocoIgnore;
import org.testcharm.dal.util.TextUtil;
import org.testcharm.interpreter.Token;
import org.testcharm.util.NumberParser;

import java.util.List;
import java.util.stream.Collectors;

public class NodeFactory {

    @GeneratedForJacocoIgnore
    private NodeFactory() {
    }

    private static final NumberParser numberParser = new NumberParser();

    public static DALNode stringSymbol(DALNode dalNode) {
        return new SymbolNode(((LiteralNode) dalNode).getValue(), SymbolNode.Type.STRING)
                .setPositionBegin(dalNode.getPositionBegin());
    }

    public static DALNode numberSymbol(DALNode dalNode) {
        return new SymbolNode(((LiteralNode) dalNode).getValue(), SymbolNode.Type.NUMBER)
                .setPositionBegin(dalNode.getPositionBegin());
    }

    public static SymbolNode symbolNode(Token token) {
        return new SymbolNode(token.getContent(), SymbolNode.Type.SYMBOL);
    }

    public static SymbolNode metaSymbolNode(Token token) {
        return new MetaSymbolNode(token.getContent());
    }

    public static SchemaComposeNode schemas(List<DALNode> nodes) {
        return new SchemaComposeNode(nodes.stream().map(SchemaNode.class::cast).collect(Collectors.toList()), false);
    }

    public static SchemaComposeNode elementSchemas(List<DALNode> nodes) {
        return new SchemaComposeNode(nodes.stream().map(SchemaNode.class::cast).collect(Collectors.toList()), true);
    }

    public static SchemaNode schema(Token token) {
        return (SchemaNode) new SchemaNode(token.getContent()).setPositionBegin(token.getPosition());
    }

    public static DALNode bracketSymbolNode(DALNode node) {
        return new SymbolNode(((LiteralNode) node).getValue(), SymbolNode.Type.BRACKET);
    }

    public static DALNode parenthesesNode(DALNode node) {
        return new ParenthesesNode(node);
    }

    public static DALNode literalString(List<Character> characters) {
        return new LiteralNode(TextUtil.join(characters));
    }

    public static DALNode relaxString(Token token) {
        return new LiteralNode(token.getContent().trim());
    }

    public static DALNode regex(List<Character> characters) {
        return new RegexNode(TextUtil.join(characters));
    }

    public static DALNode literalTrue(String token) {
        return new LiteralNode(true);
    }

    public static DALNode literalFalse(String token) {
        return new LiteralNode(false);
    }

    public static DALNode literalNull(String token) {
        return new LiteralNode(null);
    }

    public static LiteralNode literalNumber(Token token) {
        return new LiteralNode(numberParser.parseNumber(token.getContent()));
    }

    public static DALNode createVerificationGroup(List<DALNode> list) {
        if (list.size() == 1)
            return list.get(0);
        return new GroupExpression(list);
    }

    public static DALNode literalRemarkNode(DALNode node) {
        return new ConstRemarkNode(node);
    }

    public static DALNode dataRemarkNode(List<Character> characters) {
        return new DataRemarkNode(TextUtil.join(characters).trim());
    }
}
