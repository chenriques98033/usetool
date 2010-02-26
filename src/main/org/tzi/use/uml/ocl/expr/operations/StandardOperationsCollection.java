package org.tzi.use.uml.ocl.expr.operations;

import org.tzi.use.uml.ocl.expr.EvalContext;
import org.tzi.use.uml.ocl.expr.ExpInvalidException;
import org.tzi.use.uml.ocl.expr.ExpStdOp;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpressionWithValue;
import org.tzi.use.uml.ocl.type.CollectionType;
import org.tzi.use.uml.ocl.type.TupleType;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.ocl.type.TupleType.Part;
import org.tzi.use.uml.ocl.value.BagValue;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.CollectionValue;
import org.tzi.use.uml.ocl.value.IntegerValue;
import org.tzi.use.uml.ocl.value.OrderedSetValue;
import org.tzi.use.uml.ocl.value.RealValue;
import org.tzi.use.uml.ocl.value.SequenceValue;
import org.tzi.use.uml.ocl.value.SetValue;
import org.tzi.use.uml.ocl.value.UndefinedValue;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.util.Log;
import org.tzi.use.util.MultiMap;

public class StandardOperationsCollection {
	public static void registerTypeOperations(MultiMap<String, OpGeneric> opmap) {
		// operations on Collection
		OpGeneric.registerOperation(new Op_collection_size(), opmap);
		OpGeneric.registerOperation(new Op_collection_includes(), opmap);
		OpGeneric.registerOperation(new Op_collection_excludes(), opmap);
		OpGeneric.registerOperation(new Op_collection_count(), opmap);
		OpGeneric.registerOperation(new Op_collection_includesAll(), opmap);
		OpGeneric.registerOperation(new Op_collection_excludesAll(), opmap);
		OpGeneric.registerOperation(new Op_collection_isEmpty(), opmap);
		OpGeneric.registerOperation(new Op_collection_notEmpty(), opmap);
		OpGeneric.registerOperation(new Op_collection_max(), opmap);
		OpGeneric.registerOperation(new Op_collection_min(), opmap);
		OpGeneric.registerOperation(new Op_collection_sum(), opmap);
		OpGeneric.registerOperation(new Op_collection_product(), opmap);
		OpGeneric.registerOperation(new Op_collection_asSet(), opmap);
		OpGeneric.registerOperation(new Op_collection_asSequence(), opmap);
		OpGeneric.registerOperation(new Op_collection_asOrderedSet(), opmap);
		OpGeneric.registerOperation(new Op_collection_asBag(), opmap);
		
		OpGeneric.registerOperation(new Op_collection_flatten(), opmap); // USE specific
		
	}
}

// --------------------------------------------------------
//
// Collection operations.
//
// --------------------------------------------------------

/* size : Collection(T) -> Integer */
final class Op_collection_size extends OpGeneric {
	public String name() {
		return "size";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		return (params.length == 1 && params[0].isCollection()) ? TypeFactory
				.mkInteger() : null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll = (CollectionValue) args[0];
		return new IntegerValue(coll.size());
	}
}

// --------------------------------------------------------

/* includes : Collection(T2) x T1 -> Boolean, with T2 <= T1 */
final class Op_collection_includes extends OpGeneric {
	public String name() {
		return "includes";
	}

	// may test for undefined being an element of the collection
	public int kind() {
		return SPECIAL;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 2 && params[0].isCollection()) {
			CollectionType coll = (CollectionType) params[0];
			if (params[1].getLeastCommonSupertype(coll.elemType()) != null)
				return TypeFactory.mkBoolean();
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		if (args[0].isUndefined())
			return BooleanValue.FALSE;
		CollectionValue coll = (CollectionValue) args[0];
		boolean res = coll.includes(args[1]);
		return BooleanValue.get(res);
	}
}

// --------------------------------------------------------

/* excludes : Collection(T2) x T1 -> Boolean, with T2 <= T1 */
final class Op_collection_excludes extends OpGeneric {
	public String name() {
		return "excludes";
	}

	// may test for undefined being an element of the collection
	public int kind() {
		return SPECIAL;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 2 && params[0].isCollection()) {
			CollectionType coll = (CollectionType) params[0];
			if (params[1].getLeastCommonSupertype(coll.elemType()) != null)
				return TypeFactory.mkBoolean();
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		if (args[0].isUndefined())
			return BooleanValue.FALSE;
		CollectionValue coll = (CollectionValue) args[0];
		boolean res = !coll.includes(args[1]);
		return BooleanValue.get(res);
	}
}

// --------------------------------------------------------

/* count : Collection(T) x T -> Integer */
final class Op_collection_count extends OpGeneric {
	public String name() {
		return "count";
	}

	// may count occurrences of undefined in the collection
	public int kind() {
		return SPECIAL;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 2 && params[0].isCollection()) {
			CollectionType coll = (CollectionType) params[0];
			if (params[1].getLeastCommonSupertype(coll.elemType()) != null)
				return TypeFactory.mkInteger();
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		if (args[0].isUndefined())
			return new IntegerValue(0);
		CollectionValue coll = (CollectionValue) args[0];
		int res = coll.count(args[1]);
		return new IntegerValue(res);
	}
}

// --------------------------------------------------------

/* includesAll : Collection(T) x Collection(T) -> Boolean */
final class Op_collection_includesAll extends OpGeneric {
	public String name() {
		return "includesAll";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 2 && params[0].isCollection()
				&& params[1].isCollection()) {
			CollectionType coll1 = (CollectionType) params[0];
			CollectionType coll2 = (CollectionType) params[1];

			if (coll2.getLeastCommonSupertype(coll1) != null)
				return TypeFactory.mkBoolean();
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll1 = (CollectionValue) args[0];
		CollectionValue coll2 = (CollectionValue) args[1];
		boolean res = coll1.includesAll(coll2);
		return BooleanValue.get(res);
	}
}

// --------------------------------------------------------

/* excludesAll : Collection(T) x Collection(T) -> Boolean */
final class Op_collection_excludesAll extends OpGeneric {
	public String name() {
		return "excludesAll";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 2 && params[0].isCollection()
				&& params[1].isCollection()) {
			CollectionType coll1 = (CollectionType) params[0];
			CollectionType coll2 = (CollectionType) params[1];

			if (coll2.getLeastCommonSupertype(coll1) != null)
				return TypeFactory.mkBoolean();
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll1 = (CollectionValue) args[0];
		CollectionValue coll2 = (CollectionValue) args[1];
		boolean res = coll1.excludesAll(coll2);
		return BooleanValue.get(res);
	}
}

// --------------------------------------------------------

/* isEmpty : Collection(T) -> Boolean */
final class Op_collection_isEmpty extends OpGeneric {
	public String name() {
		return "isEmpty";
	}

	public int kind() {
		return PREDICATE;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		return (params.length == 1 && params[0].isCollection()) ? TypeFactory
				.mkBoolean() : null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll = (CollectionValue) args[0];
		return BooleanValue.get(coll.isEmpty());
	}
}

// --------------------------------------------------------

/* notEmpty : Collection(T) -> Boolean */
final class Op_collection_notEmpty extends OpGeneric {
	public String name() {
		return "notEmpty";
	}

	public int kind() {
		return PREDICATE;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		return (params.length == 1 && params[0].isCollection()) ? TypeFactory
				.mkBoolean() : null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll = (CollectionValue) args[0];
		return BooleanValue.get(!coll.isEmpty());
	}
}

// --------------------------------------------------------

/* sum : Collection(T) -> T */
final class Op_collection_sum extends OpGeneric {
	public String name() {
		return "sum";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType c = (CollectionType) params[0];
			if (c.elemType().isInteger())
				return TypeFactory.mkInteger();
			else if (c.elemType().isReal())
				return TypeFactory.mkReal();
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll = (CollectionValue) args[0];
		boolean isIntegerCollection = coll.elemType().isInteger();

		if (isIntegerCollection) {
			int isum = 0;
			for (Value v : coll) {
				if (v.isUndefined())
					return new UndefinedValue(TypeFactory.mkInteger());
				isum += ((IntegerValue) v).value();
			}
			return new IntegerValue(isum);
		} else {
			double rsum = 0.0;

			for (Value v : coll) {
				if (v.isUndefined())
					return new UndefinedValue(TypeFactory.mkReal());
				if (v.isInteger())
					rsum += ((IntegerValue) v).value();
				else
					rsum += ((RealValue) v).value();
			}
			return new RealValue(rsum);
		}
	}
}

/* product : Collection(T) x Collection(T2) -> Set(Tuple(first: T, second : T2)) */
final class Op_collection_product extends OpGeneric {
	public String name() {
		return "product";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 2 && params[0].isCollection()
				&& params[1].isCollection()) {
			CollectionType c = (CollectionType) params[0];
			CollectionType c2 = (CollectionType) params[1];

			Part[] parts = new Part[2];
			parts[0] = new Part("first", c.elemType());
			parts[1] = new Part("second", c2.elemType());

			TupleType tupleType = TypeFactory.mkTuple(parts);
			return TypeFactory.mkSet(tupleType);
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		if (args[0].isUndefined() || args[1].isUndefined())
			return new UndefinedValue(resultType);

		CollectionValue col1 = (CollectionValue) args[0];
		CollectionValue col2 = (CollectionValue) args[1];

		return col1.product(col2);
	}
}

// --------------------------------------------------------

/* flatten : C1(C2(T)) -> C1(T) */
final class Op_collection_flatten extends OpGeneric {
	public String name() {
		return "flatten";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType c1 = (CollectionType) params[0];
			if (c1.elemType().isCollection()) {
				CollectionType c2 = (CollectionType) c1.elemType();
				if (c1.isSet())
					return TypeFactory.mkSet(c2.elemType());
				else if (c1.isBag())
					return TypeFactory.mkBag(c2.elemType());
				else if (c1.isSequence() /* && c2.isSequence() */)
					return TypeFactory.mkSequence(c2.elemType());
				else
					return c1.elemType();
			} else {
				return c1;
			}
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue coll = (CollectionValue) args[0];

		for (Value elem : coll) {
			if (elem.isUndefined())
				return new UndefinedValue(resultType);
		}
		if (coll.isBag())
			return ((BagValue) coll).flatten();
		else if (coll.isSet())
			return ((SetValue) coll).flatten();
		else if (coll.isSequence())
			return ((SequenceValue) coll).flatten();
		else if (coll.isOrderedSet())
			return ((OrderedSetValue) coll).flatten();
		else
			throw new RuntimeException("Unexpected collection type `"
					+ coll.type() + "'.");
	}
}

/* asBag : Collection(T) -> Bag(T) */
final class Op_collection_asBag extends OpGeneric {
	public String name() {
		return "asBag";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType col = (CollectionType) params[0];
			return TypeFactory.mkBag(col.elemType());
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue col = (CollectionValue) args[0];
		return col.asBag();
	}
}

/* asSet : Collection(T) -> Set(T) */
final class Op_collection_asSet extends OpGeneric {
	public String name() {
		return "asSet";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType col = (CollectionType) params[0];
			return TypeFactory.mkSet(col.elemType());
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue col = (CollectionValue) args[0];
		return col.asSet();
	}
}

/* asSequence : Collection(T) -> Sequence(T) */
final class Op_collection_asSequence extends OpGeneric {
	public String name() {
		return "asSequence";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType col = (CollectionType) params[0];
			return TypeFactory.mkSequence(col.elemType());
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue col = (CollectionValue) args[0];
		return col.asSequence();
	}
}

/* asOrderedSet : Collection(T) -> OrderedSet(T) */
final class Op_collection_asOrderedSet extends OpGeneric {
	public String name() {
		return "asOrderedSet";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType col = (CollectionType) params[0];
			return TypeFactory.mkOrderedSet(col.elemType());
		}
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue col = (CollectionValue) args[0];
		return col.asOrderedSet();
	}
}

/* max : Collection(T) -> T */
final class Op_collection_max extends OpGeneric {
	public String name() {
		return "max";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType t = (CollectionType)params[0];

			// Check if basic type supports max operation
			if (ExpStdOp.exists("max", new Type[]{t.elemType(), t.elemType()}))
				return t.elemType();
		}
		
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue col = (CollectionValue) args[0];
		
		Value max = new UndefinedValue(resultType);
		boolean first = true;
		
		for (Value v : col) {
			if (first) {
				max = v;
				first = false;
			} else {
				try {
					ExpStdOp op = ExpStdOp.create("max", 
							new Expression[]{new ExpressionWithValue(max), new ExpressionWithValue(v)});
					max = op.eval(ctx);
				} catch (ExpInvalidException e) {
					Log.error(e);
					return new UndefinedValue(resultType);
				}
			}
		}
		
		return max;
	}
}

/* min : Collection(T) -> T */
final class Op_collection_min extends OpGeneric {
	public String name() {
		return "min";
	}

	public int kind() {
		return OPERATION;
	}

	public boolean isInfixOrPrefix() {
		return false;
	}

	public Type matches(Type params[]) {
		if (params.length == 1 && params[0].isCollection()) {
			CollectionType t = (CollectionType)params[0];

			// Check if basic type supports max operation
			if (ExpStdOp.exists("min", new Type[]{t.elemType(), t.elemType()}))
				return t.elemType();
		}
		
		return null;
	}

	public Value eval(EvalContext ctx, Value[] args, Type resultType) {
		CollectionValue col = (CollectionValue) args[0];
		
		Value max = new UndefinedValue(resultType);
		boolean first = true;
		
		for (Value v : col) {
			if (first) {
				max = v;
				first = false;
			} else {
				try {
					ExpStdOp op = ExpStdOp.create("min", 
							new Expression[]{new ExpressionWithValue(max), new ExpressionWithValue(v)});
					max = op.eval(ctx);
				} catch (ExpInvalidException e) {
					Log.error(e);
					return new UndefinedValue(resultType);
				}
			}
		}
		
		return max;
	}
}