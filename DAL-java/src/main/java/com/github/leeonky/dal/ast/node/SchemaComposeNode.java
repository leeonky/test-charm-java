package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import java.util.List;
import java.util.stream.Collector;

import static com.github.leeonky.dal.runtime.DalException.locateError;
import static com.github.leeonky.dal.runtime.ExpressionException.opt1;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class SchemaComposeNode extends DALNode {
    private final List<SchemaNode> schemas;
    private final boolean isList;

    public SchemaComposeNode(List<SchemaNode> schemas, boolean isList) {
        this.schemas = schemas;
        this.isList = isList;
    }

    @Override
    public String inspect() {
        Collector<CharSequence, ?, String> joining = isList ? joining(" / ", "[", "]") : joining(" / ");
        return schemas.stream().map(SchemaNode::inspect).collect(joining);
    }

    public Data verify(DALNode input, DALRuntimeContext context) {
        Data inputData = input.evaluateData(context);
        String inspect = input.inspect();
        return context.data(() -> {
            try {
                List<Object> instanceBySchema = schemas.stream().map(schemaNode ->
                        verifyAndConvertAsSchemaType(context, schemaNode, inspect, inputData)).collect(toList());
                return instanceBySchema.get(instanceBySchema.size() - 1);
            } catch (DalRuntimeException e) {
                throw locateError(e, getPositionBegin());
            }
        }, schemas.get(0).inspect(), isList);
    }

    private Object verifyAndConvertAsSchemaType(DALRuntimeContext context, SchemaNode schemaNode,
                                                String inputInspect, Data inputData) {
        if (isList) {
            DALCollection<Object> collection = opt1(inputData::list).wraps().map((index, data) ->
                    convertViaSchema(context, schemaNode, data, format("%s[%d]", inputInspect, index)));
            //get size to avoid lazy mode, should verify element with schema
            collection.collect();
            return collection;
        } else
            return convertViaSchema(context, schemaNode, inputData, inputInspect);
    }

    private Object convertViaSchema(DALRuntimeContext context, SchemaNode schemaNode, Data element, String input) {
        try {
            return schemaNode.getValueConstructorViaSchema(context).apply(element, context);
        } catch (IllegalTypeException exception) {
            throw new AssertionFailure(exception.assertionFailureMessage(input.isEmpty() ? input : input + " ",
                    schemaNode), schemaNode.getPositionBegin());
        }
    }
}
