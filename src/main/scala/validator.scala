import ast._
import waccErrors._
import java.io.File
import scala.io.Source
import scala.collection.mutable

object validator {

  val nullPos: (Int, Int) = (-1, -1)
  val waccPrefix = "wacc_"

  // char array in a string array is valid
  // string[] = [char[]]
  // string[] != char[][]
  // Only weakening is char[] = string in arrays?
  // yeah string cannot be weakened to char[]
  // Which section of the spec does it say this? Alejandro:2.1.2
  def sameType(t1: Type, t2: Type): Boolean = {
    if (t1 == t2) {
      true
    } else (t1, t2) match {
      case (ArrayType(arrt1), ArrayType(arrt2)) => sameType(arrt1, arrt2)
      case (PairType(t1l, t1r), PairType(t2l, t2r)) => sameType(t1l, t2l) && sameType(t1r, t2r)
      case (PairType(_, _), Pair()) => true
      case (Pair(), PairType(_, _)) => true
      case (StringType(), ArrayType(CharType())) => true
      case _ => if (t1 == AnyType || t2 == AnyType) true else false
    }
  }

  def weakingPermitted(t1: Type, t2: Type): Boolean = {
    if (t1 == t2) {
      true
    } else (t1, t2) match {
      case (ArrayType(CharType()), StringType()) => true
      case (StringType(), ArrayType(CharType())) => true
      case (ArrayType(arrt1), ArrayType(arrt2)) => weakingPermitted(arrt1, arrt2)
      case (PairType(t1l, t1r), PairType(t2l, t2r)) => weakingPermitted(t1l, t2l) && weakingPermitted(t1r, t2r)
      case (PairType(_, _), Pair()) => true
      case (Pair(), PairType(_, _)) => true
      case _ => if (t1 == AnyType || t2 == AnyType) true else false
    }
  }

  // Add to the errors generated by a WACC file after compilation to be returned to the user
  def semanticErrorOccurred(reason: String, pos: (Int, Int))(implicit errors: mutable.ListBuffer[Error], source: String, waccLines: Array[String]): Unit = {
    errors += SemanticError.genError(reason, pos)
  }

  // Check that any Expression passed in as a LValue is of and/or contain the expected type(s)
  def checkType(expr: LValue)(implicit symTable: mutable.Map[String, Type]): Type = {
    expr match {
      case Ident(name) => symTable.getOrElse(name, NoTypeExists)
      case ArrayElem(id, _) => checkType(id: Expr) match {
        case ArrayType(arrTyp) => arrTyp
        case _ => NoTypeExists
      }
      case PairFst(value) => checkType(value) match {
        case Pair() => AnyType
        case PairType(pairTyp, _) => pairTyp
        case _ => NoTypeExists
      }
      case PairSnd(value) => checkType(value) match {
        case Pair() => AnyType
        case PairType(_, pairTyp) => pairTyp
        case _ => NoTypeExists
      }
    }
  }

  // Check that any Expression passed in as a RValue is of and/or contain the expected type(s)
  def checkType(expr: RValue)(implicit symTable: mutable.Map[String, Type], funcTable: List[Func]): Type = expr match {
    case exp: Expr => checkType(exp)
    case NewPair(exp1, exp2) =>
      val exp1Type: PairElemType = checkType(exp1) match {
        case PairType(_, _) => Pair()(nullPos)
        case pairType: PairElemType => pairType
        case _ => NoTypeExists
      }
      val exp2Type: PairElemType = checkType(exp2) match {
        case PairType(_, _) => Pair()(nullPos)
        case pairType: PairElemType => pairType
        case _ => NoTypeExists
      }
      PairType(exp1Type, exp2Type)(null)
    case Call(ident, _) => funcTable.find(x => x.ident.name == ident.name) match {
      case Some(value) => value.typ
      case None => NoTypeExists
    }
    case PairFst(value) => checkType(value) match {
      case Pair() => AnyType
      case PairType(pairTyp, _) => pairTyp
      case _ => NoTypeExists
    }
    case PairSnd(value) => checkType(value) match {
      case Pair() => AnyType
      case PairType(_, pairTyp) => pairTyp
      case _ => NoTypeExists
    }
    case ArrayLit(elems) => if (elems.isEmpty) ArrayType(AnyType)(nullPos) else {
      val arrayElementTypes: List[Type] = elems.map(checkType)

      if (arrayElementTypes.contains(StringType()((-1, -1)))) {
        ArrayType(StringType()(nullPos))(nullPos)
      } else {
        ArrayType(arrayElementTypes.head)(nullPos)
      }
    }
  }

  // Check that any Expression passed in as a BinOp or UnOp is of and/or contain the expected type(s)
  def checkType(expr: Expr)(implicit symTable: mutable.Map[String, Type]): Type = {
    expr match {
      case Sub(_, _) => IntType()(nullPos)
      case Add(_, _) => IntType()(nullPos)
      case Mul(_, _) => IntType()(nullPos)
      case Div(_, _) => IntType()(nullPos)
      case Mod(_, _) => IntType()(nullPos)
      case GT(_, _) => BoolType()(nullPos)
      case GTE(_, _) => BoolType()(nullPos)
      case LT(_, _) => BoolType()(nullPos)
      case LTE(_, _) => BoolType()(nullPos)
      case Eq(_, _) => BoolType()(nullPos)
      case NEq(_, _) => BoolType()(nullPos)
      case And(_, _) => BoolType()(nullPos)
      case Or(_, _) => BoolType()(nullPos)
      case Not(_) => BoolType()(nullPos)
      case Len(_) => IntType()(nullPos)
      case Neg(_) => IntType()(nullPos)
      case Ord(_) => IntType()(nullPos)
      case Chr(_) => CharType()(nullPos)
      case Plus(_) => IntType()(nullPos)
      case ArrayElem(id, exprs) =>
        var arrayType = checkType(id: Expr)
        for (expr <- exprs) {
          checkType(expr) match {
            case IntType() => arrayType = arrayType match {
              case ArrayType(a) => a
              case _ => return arrayType
            }
            case _ => return NoTypeExists
          }
        }
        arrayType
      case BoolLit(_) => BoolType()(nullPos)
      case IntLit(_) => IntType()(nullPos)
      case CharLit(_) => CharType()(nullPos)
      case Ident(name) => symTable.getOrElse(name, NoTypeExists)
      case StrLit(_) => StringType()(nullPos)
      case PairLiter() => PairType(AnyType, AnyType)(nullPos)
    }
  }

  // Check that any Expression passed in as a LValue is syntactically sound according to the WACC specification
  def checkExpr(expr: LValue, varsInScope: Map[String, String])(implicit errors: mutable.ListBuffer[Error],
                                                                symTable: mutable.Map[String, Type],
                                                                funcTable: List[Func], source: String,
                                                                waccLines: Array[String]): LValue = {
    expr match {
      case Ident(name) =>
        if (!varsInScope.contains(name)) {
          semanticErrorOccurred(s"Identifier not in scope: $name", expr.pos)
          expr
        } else {
          new Ident(varsInScope(name))(expr.pos)
        }
      case ArrayElem(id, indexes) =>
        checkArrayIndex(indexes, symTable) match {
          case Some((err, pos)) => semanticErrorOccurred(err, pos)
          case _ =>
        }

        id match {
          case ArrayElem(_, _) =>
            checkExpr(id: Expr, varsInScope) match {
              case newId: Ident => new ArrayElem(newId, indexes)(expr.pos)
              case _ =>
                semanticErrorOccurred(s"Array identifier evaluates to incorrect type: $id", id.pos)
                expr
            }
          case Ident(name) =>
            symTable.get(varsInScope.getOrElse(name, "")) match {
              case Some(value) => value match {
                case ArrayType(_) => new ArrayElem(new Ident(varsInScope(name))(id.pos), indexes)(expr.pos)
                case _ =>
                  semanticErrorOccurred(s"Attempting to access array element from non-array identifier: $name", id.pos)
                  expr
              }
              case None =>
                semanticErrorOccurred(s"Identifier not in scope: $name", id.pos)
                expr
            }
        }
      case PairFst(value) => new PairFst(checkExpr(value, varsInScope))(expr.pos)
      case PairSnd(value) => new PairSnd(checkExpr(value, varsInScope))(expr.pos)
    }
  }

  private def checkArrayIndex(exprs: List[Expr], varsInScope: mutable.Map[String, Type])(implicit errors: mutable.ListBuffer[Error],
                                                                                         symTable: mutable.Map[String, Type],
                                                                                         funcTable: List[Func], source: String,
                                                                                         waccLines: Array[String]): Option[(String, (Int, Int))] = {
    for (expr <- exprs) {
      checkType(expr)(varsInScope) match {
        case IntType() =>
        case _ => Option("Array Indexes must be of type int", expr.pos)
      }
    }
    None
  }

  private def getDimension(array:Type): Int = {
    array match {
      case ArrayType(x) => 1 + getDimension(x)
      case _ => 0
    }
  }



  // Check that any Expression passed in as a RValue is syntactically sound according to the WACC specification
  def checkExpr(expr: RValue, varsInScope: Map[String, String])(implicit errors: mutable.ListBuffer[Error],
                                                                symTable: mutable.Map[String, Type],
                                                                funcTable: List[Func], source: String,
                                                                waccLines: Array[String]): RValue = {
    expr match {
      case exp: Expr => checkExpr(exp, varsInScope)
      case NewPair(exp1, exp2) =>
        val newExp1 = checkExpr(exp1, varsInScope)
        val newExp2 = checkExpr(exp2, varsInScope)
        checkType(newExp1) match {
          case PairType(_, _) =>
          case _: PairElemType =>
          case _ => semanticErrorOccurred("Invalid data type for element in newpair", exp1.pos)
        }
        checkType(newExp2) match {
          case PairType(_, _) =>
          case _: PairElemType =>
          case _ => semanticErrorOccurred("Invalid data type for element in newpair", exp2.pos)
        }
        new NewPair(newExp1, newExp2)(expr.pos)
      case Call(id, params) =>
        val newParams = params.map(checkExpr(_, varsInScope))
        val newId = Ident(waccPrefix + id.name)(id.pos)
        funcTable.find(x => x.ident.name == newId.name) match {
          case Some(funcCalled) =>
            if (funcCalled.paramList.length != newParams.length) {
              semanticErrorOccurred(s"Call to function ${id.name} has the incorrect number of arguments", expr.pos)
            }
            (funcCalled.paramList zip newParams).foreach({ case (x, y) =>
              if (!sameType(x.typ, checkType(y))) {
                semanticErrorOccurred(s"Argument ${x.ident.name} in the call to function ${id.name} has the incorrect type. Expected ${x.typ}. Received ${checkType(y)}", y.pos)
              }
            })
          case None => semanticErrorOccurred(s"Unrecognised function identifier ${id.name}", id.pos)
        }
        new Call(newId, newParams)(expr.pos)
      case ArrayLit(elems) =>
        val newElems = elems.map(checkExpr(_, varsInScope))
        // Here we need a different function, not sameType.
        // To allow char[] as a valid element in string[]
        // Agreed
        // How do you get the type of the array?
        newElems.foreach(x =>
          // sameType() // Does not allow string = char[]
          // Char[] Elem = string elem
          // weakeningPermitted() // Does allow string = char[]
          if (!weakingPermitted(checkType(x), checkType(newElems.head)))
            semanticErrorOccurred("Elements in array literal have different types", expr.pos))
        new ArrayLit(newElems)(expr.pos)
      case PairFst(value) => new PairFst(checkExpr(value, varsInScope))(expr.pos)
      case PairSnd(value) => new PairSnd(checkExpr(value, varsInScope))(expr.pos)
    }
  }

  // Check that other Expressions passed in as are syntactically sound according to the WACC specification
  def checkExpr(expr: Expr, varsInScope: Map[String, String])(implicit errors: mutable.ListBuffer[Error],
                                                              symTable: mutable.Map[String, Type],
                                                              funcTable: List[Func], source: String,
                                                              waccLines: Array[String]): Expr = {
    expr match {
      case binOp: BinOpp => checkBinOp(binOp, varsInScope)
      case unOp: UnOpp => checkUnOp(unOp, varsInScope)
      case ArrayElem(id, indexes) =>
        checkArrayIndex(indexes, symTable) match {
          case Some((err, pos)) => semanticErrorOccurred(err, pos)
          case _ =>
        }
        val arrDim = getDimension(checkType(checkExpr(id: Expr, varsInScope)))
        if (arrDim < indexes.length) {
          semanticErrorOccurred(s"Array invalid dimensions do not match: indexes passed in are $indexes, expected ${indexes.length} , found $arrDim", id.pos)
        }

        id match {
          case ArrayElem(_, _) =>
            checkExpr(id: Expr, varsInScope) match {
              case newId: Ident => new ArrayElem(newId, indexes)(expr.pos)
              case _ =>
                semanticErrorOccurred(s"Array identifier evaluates to the wrong type", id.pos)
                expr
            }
          case Ident(name) =>
            symTable.get(varsInScope.getOrElse(name, "")) match {
              case Some(value) => value match {
                case ArrayType(_) => new ArrayElem(new Ident(varsInScope(name))(id.pos), indexes)(expr.pos)
                case _ =>
                  semanticErrorOccurred(s"Attempting to access array element from non-array identifier $name", id.pos)
                  expr
              }
              case None =>
                semanticErrorOccurred(s"Identifier not in scope: $name", id.pos)
                expr
            }
        }
      case Ident(name) =>
        if (!varsInScope.contains(name)) {
          semanticErrorOccurred(s"Identifier not in scope: $name", expr.pos)
          expr
        } else {
          new Ident(varsInScope(name))(expr.pos)
        }
      case _ => expr
    }
  }

  // Check that Unary Operators are syntactically sound according to the WACC specification
  def checkUnOp(expr: UnOpp, varsInScope: Map[String, String])(implicit errors: mutable.ListBuffer[Error],
                                                               symTable: mutable.Map[String, Type],
                                                               funcTable: List[Func], source: String,
                                                               waccLines: Array[String]): UnOpp = {
    val inside = checkExpr(expr.x, varsInScope)

    def returnsIntType(op: String): Unit = {
      checkType(inside) match {
        case IntType() =>
        case _ => semanticErrorOccurred(s"Argument $op is not of type Int", inside.pos)
      }
    }

    def returnsCharType(op: String): Unit = {
      checkType(inside) match {
        case CharType() =>
        case _ => semanticErrorOccurred(s"Argument $op is not of type Char", inside.pos)
      }
    }

    def returnsBoolType(op: String): Unit = {
      checkType(inside) match {
        case BoolType() =>
        case _ => semanticErrorOccurred(s"Argument $op is not of type Bool", inside.pos)
      }
    }

    def returnsSeqType(op: String): Unit = {
      checkType(inside) match {
        case ArrayType(_) =>
        case StringType() =>
        case _ => semanticErrorOccurred(s"Argument $op is not of type String", inside.pos)
      }
    }

    expr match {
      case Chr(_) =>
        returnsIntType("chr")
        new Chr(inside)(expr.pos)
      case Len(_) =>
        returnsSeqType("len")
        new Len(inside)(expr.pos)
      case Neg(_) =>
        returnsIntType("neg")
        new Neg(inside)(expr.pos)
      case Not(_) =>
        returnsBoolType("!")
        new Not(inside)(expr.pos)
      case Ord(_) =>
        returnsCharType("ord")
        new Ord(inside)(expr.pos)
    }
  }

  // Check that Binary Operators are syntactically sound according to the WACC specification
  def checkBinOp(expr: BinOpp, varsInScope: Map[String, String])(implicit errors: mutable.ListBuffer[Error],
                                                                 symTable: mutable.Map[String, Type],
                                                                 funcTable: List[Func], source: String,
                                                                 waccLines: Array[String]): BinOpp = {
    val newX = checkExpr(expr.x, varsInScope)
    val newY = checkExpr(expr.y, varsInScope)

    def returnsIntType(op: String): Unit = {
      checkType(newX) match {
        case IntType() =>
        case _ => semanticErrorOccurred(s"Left expression in $op is not of type Int, is ${checkType(newX)} instead", newX.pos)
      }
      checkType(newY) match {
        case IntType() =>
        case _ => semanticErrorOccurred(s"Right expression in $op is not of type Int", newY.pos)
      }
    }

    def returnsBoolType(op: String): Unit = {
      checkType(newX) match {
        case BoolType() =>
        case _ => semanticErrorOccurred(s"Left expression in $op is not of type Bool", newX.pos)
      }
      checkType(newY) match {
        case BoolType() =>
        case _ => semanticErrorOccurred(s"Right expression in $op is not of type Bool", newY.pos)
      }
    }

    def returnsIntOrCharType(op: String): Unit = {
      val exp1Typ = checkType(newX)
      exp1Typ match {
        case IntType() =>
        case CharType() =>
        case _ => semanticErrorOccurred(s"Left expression in $op is not of type Int nor type Char", newX.pos)
      }
      val exp2Typ = checkType(newY)
      exp2Typ match {
        case IntType() =>
        case CharType() =>
        case _ => semanticErrorOccurred(s"Right expression in $op is not of type Int nor type Char", newY.pos)
      }
      if (!sameType(exp1Typ, exp2Typ)) {
        semanticErrorOccurred(s"Two sides of $op has different types, left is of type $exp1Typ, right is of type $exp2Typ", expr.pos)
      }
    }

    def returnsSameType(op: String): Unit = {
      val exp1Typ = checkType(newX)
      val exp2Typ = checkType(newY)
      if (!sameType(exp1Typ, exp2Typ)) {
        semanticErrorOccurred(s"Two sides of $op has different types, left is of type $exp1Typ, right is of type $exp2Typ", expr.pos)
      }
    }

    expr match {
      case Sub(_, _) =>
        returnsIntType("subtraction")
        new Sub(newX, newY)(expr.pos)
      case Add(_, _) =>
        returnsIntType("addition")
        new Add(newX, newY)(expr.pos)
      case Mul(_, _) =>
        returnsIntType("multiplication")
        new Mul(newX, newY)(expr.pos)
      case Div(_, _) =>
        returnsIntType("division")
        new Div(newX, newY)(expr.pos)
      case Mod(_, _) =>
        returnsIntType("modulusation")
        new Mod(newX, newY)(expr.pos)
      case GT(_, _) =>
        returnsIntOrCharType("\'>\'")
        new GT(newX, newY)(expr.pos)
      case GTE(_, _) =>
        returnsIntOrCharType("\'>=\'")
        new GTE(newX, newY)(expr.pos)
      case LT(_, _) =>
        returnsIntOrCharType("\'<\'")
        new LT(newX, newY)(expr.pos)
      case LTE(_, _) =>
        returnsIntOrCharType("\'<=\'")
        new LTE(newX, newY)(expr.pos)
      case Eq(_, _) =>
        returnsSameType("\'==\'")
        new Eq(newX, newY)(expr.pos)
      case NEq(_, _) =>
        returnsSameType("\'!=\'")
        new NEq(newX, newY)(expr.pos)
      case And(_, _) =>
        returnsBoolType("and operation")
        new And(newX, newY)(expr.pos)
      case Or(_, _) =>
        returnsBoolType("or operation")
        new Or(newX, newY)(expr.pos)
    }
  }

  def checkStatements(stats: List[Stat], varsInScope: Map[String, String], returnType: Type,
                      scopePrefix: String)(implicit errors: mutable.ListBuffer[Error],
                                           symTable: mutable.Map[String, Type],
                                           funcTable: List[Func], source: String,
                                           waccLines: Array[String]): List[Stat] = {

    var localSymTable: Map[String, String] = Map.empty[String, String]
    val newStats: mutable.ListBuffer[Stat] = mutable.ListBuffer.empty[Stat]
    var scopeIndex = 0

    for (stat <- stats) {
      val checkedStat: Stat = stat match {
        case Skip() => stat
        case Declaration(idType, id, value) =>
          val newValue = checkExpr(value, varsInScope ++ localSymTable)
          val newIdName = scopePrefix ++ id.name

          if (localSymTable.contains(id.name)) {
            semanticErrorOccurred(s"Variable named '${id.name}' is already defined", id.pos)
          } else if (!sameType(idType, checkType(newValue))) {
            semanticErrorOccurred(s"Type mismatch in declaration of ${id.name}: expected $idType, found ${checkType(newValue)}", stat.pos)
          }
          localSymTable = localSymTable + (id.name -> newIdName)
          symTable += (newIdName -> idType)
          new Declaration(idType, new Ident(newIdName)(id.pos), newValue)(stat.pos)
        case Assign(lVal, rVal) =>
          val newLVal = checkExpr(lVal, varsInScope ++ localSymTable)
          val newRVal = checkExpr(rVal, varsInScope ++ localSymTable)

          if (!sameType(checkType(newLVal), checkType(newRVal))) {
            semanticErrorOccurred(s"Type mismatch in assignment: expected ${checkType(newLVal)}, found ${checkType(newRVal)}", stat.pos)
          } else if (checkType(newLVal) == AnyType && checkType(newRVal) == AnyType) {
            semanticErrorOccurred("Types unclear on both sides of assignment", stat.pos)
          }
          new Assign(newLVal, newRVal)(stat.pos)
        case Read(expr) =>
          val newExpr = checkExpr(expr, varsInScope ++ localSymTable)
          checkType(newExpr) match {
            case IntType() =>
            case CharType() =>
            case StringType() =>
            case _ => semanticErrorOccurred("Variable attempting read has incorrect type, can only be of type Int, Char or String", stat.pos)
          }
          new Read(newExpr)(stat.pos)
        case Free(expr) =>
          val newExpr = checkExpr(expr, varsInScope ++ localSymTable)
          checkType(newExpr) match {
            case PairType(_, _) | ArrayType(_) =>
            case _ => semanticErrorOccurred("Only Pair and Array types can be freed", stat.pos)
          }
          new Free(newExpr)(stat.pos)
        case Return(expr) =>
          val newExpr = checkExpr(expr, varsInScope ++ localSymTable)
          if (returnType == null) {
            semanticErrorOccurred("Return is misused in main program", stat.pos)
          } else if (!sameType(checkType(newExpr), returnType)) {
            semanticErrorOccurred(s"Type mismatch in Return: expected $returnType, found ${checkType(expr)}", stat.pos)
          }
          new Return(newExpr)(stat.pos)
        case Exit(expr) =>
          val newExpr = checkExpr(expr, varsInScope ++ localSymTable)
          checkType(newExpr) match {
            case IntType() =>
            case _ => semanticErrorOccurred("Exit code defined is not of type Int", newExpr.pos)
          }
          new Exit(newExpr)(stat.pos)
        case Print(expr) => new Print(checkExpr(expr, varsInScope ++ localSymTable))(stat.pos)
        case Println(expr) => new Println(checkExpr(expr, varsInScope ++ localSymTable))(stat.pos)
        case If(expr, thenStat, elseStat) =>
          val newExpr = checkExpr(expr, varsInScope ++ localSymTable)
          checkType(newExpr) match {
            case BoolType() =>
            case _ => semanticErrorOccurred("Condition for If statement is not of type Bool", expr.pos)
          }
          val newThenStat = checkStatements(thenStat, varsInScope ++ localSymTable, returnType, s"$scopePrefix${scopeIndex}ifthen-")
          val newElseStat = checkStatements(elseStat, varsInScope ++ localSymTable, returnType, s"$scopePrefix${scopeIndex}ifelse-")
          scopeIndex += 1
          new If(newExpr, newThenStat, newElseStat)(stat.pos)
        case While(expr, whileBody) =>
          val newExpr = checkExpr(expr, varsInScope ++ localSymTable)
          checkType(newExpr) match {
            case BoolType() =>
            case _ => semanticErrorOccurred("Condition for While statement is not of type Bool", expr.pos)
          }
          val newBody = checkStatements(whileBody, varsInScope ++ localSymTable, returnType, s"$scopePrefix${scopeIndex}while-")
          scopeIndex += 1
          new While(newExpr, newBody)(stat.pos)
        case Scope(body) =>
          val newBody = checkStatements(body, varsInScope ++ localSymTable, returnType, s"$scopePrefix%${scopeIndex}subscope-")
          scopeIndex += 1
          new Scope(newBody)(stat.pos)
      }
      newStats += checkedStat
    }
    newStats.toList
  }

  def checkSemantics(inProg: Prog, file: String): (List[Error], Prog, mutable.Map[String, Type]) = {
    implicit val funcTable: List[Func] = inProg.funcs.map {
      case x@Func(funcType, id, params, funcStats) => new Func(funcType, Ident(waccPrefix + id.name)(id.pos), params, funcStats)(x.pos)
    }
    implicit val fileName: String = file
    val fileSource = Source.fromFile(new File(file))
    implicit val fileContents: Array[String] = fileSource.getLines().toArray
    fileSource.close()
    implicit val symTable: mutable.Map[String, Type] = mutable.LinkedHashMap[String, Type]()
    implicit val errors: mutable.ListBuffer[Error] = mutable.ListBuffer.empty[Error]

    var tempFuncTable: List[Func] = Nil
    val newFuncs = funcTable.map(x => {
      if (tempFuncTable.exists(y => y.ident.name == x.ident.name)) {
        semanticErrorOccurred(s"Duplicated function declaration: ${x.ident.name}", x.pos)
      } else {
        tempFuncTable = tempFuncTable :+ x
      }
      var argList: List[Param] = Nil
      x.paramList.foreach(a => if (argList.exists(b => a.ident.name == b.ident.name)) {
        semanticErrorOccurred(s"Duplicated function argument ${a.ident.name} in function ${x.ident.name}", a.pos)
      } else {
        argList = argList :+ a
      })
      val funcScopePrefix = s"func-${x.ident.name}-"
      x.paramList.foreach(y => symTable += (funcScopePrefix ++ "param-" ++ y.ident.name) -> y.typ)
      new Func(x.typ, x.ident, x.paramList,
        checkStatements(x.stats, x.paramList.map(y => y.ident.name -> (funcScopePrefix ++ "param-" ++ y.ident.name)).toMap, x.typ, funcScopePrefix))(x.pos)
    })
    val newProg = new Prog(newFuncs, checkStatements(inProg.stats, Map.empty, null, "main-"))(inProg.pos)
    (errors.toList, newProg, symTable)
  }
}
