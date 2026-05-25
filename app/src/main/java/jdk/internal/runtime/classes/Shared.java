package jdk.internal.runtime.classes;

public final class Shared {

    private final java.lang.Object classFacade;
    private final java.lang.Object stringFacade;
    private final java.lang.Object threadFacade;
    private final java.lang.Object runtimeFacade;
    private final java.lang.Object compilerFacade;
    private final java.lang.Object threadGroupFacade;
    private final java.lang.Object systemFacade;
    private final java.lang.Object classLoaderFacade;
    private final java.lang.expression.Lambda<java.lang.Object, java.lang.Object> expressionLambda;
    private final java.lang.expression.Expression<java.lang.Object, java.lang.Object> expression;
    private final java.lang.expression.Chain<java.lang.Object, java.lang.Object> chain;
    private final java.lang.expression.Tracing<java.lang.Object, java.lang.Object> tracing;

    public Shared(
        java.lang.Object classFacade,
        java.lang.Object stringFacade,
        java.lang.Object threadFacade,
        java.lang.Object runtimeFacade,
        java.lang.Object compilerFacade,
        java.lang.Object threadGroupFacade,
        java.lang.Object systemFacade,
        java.lang.Object classLoaderFacade,
        java.lang.expression.Lambda<java.lang.Object, java.lang.Object> expressionLambda,
        java.lang.expression.Expression<java.lang.Object, java.lang.Object> expression,
        java.lang.expression.Chain<java.lang.Object, java.lang.Object> chain,
        java.lang.expression.Tracing<java.lang.Object, java.lang.Object> tracing
    ) {
        this.classFacade = classFacade;
        this.stringFacade = stringFacade;
        this.threadFacade = threadFacade;
        this.runtimeFacade = runtimeFacade;
        this.compilerFacade = compilerFacade;
        this.threadGroupFacade = threadGroupFacade;
        this.systemFacade = systemFacade;
        this.classLoaderFacade = classLoaderFacade;
        this.expressionLambda = expressionLambda;
        this.expression = expression;
        this.chain = chain;
        this.tracing = tracing;
    }

    public java.lang.Object getClassFacade() {
        return this.classFacade;
    }

    public java.lang.Object getStringFacade() {
        return this.stringFacade;
    }

    public java.lang.Object getThreadFacade() {
        return this.threadFacade;
    }

    public java.lang.Object getRuntimeFacade() {
        return this.runtimeFacade;
    }

    public java.lang.Object getCompilerFacade() {
        return this.compilerFacade;
    }

    public java.lang.Object getThreadGroupFacade() {
        return this.threadGroupFacade;
    }

    public java.lang.Object getSystemFacade() {
        return this.systemFacade;
    }

    public java.lang.Object getClassLoaderFacade() {
        return this.classLoaderFacade;
    }

    public java.lang.expression.Lambda<java.lang.Object, java.lang.Object> getExpressionLambda() {
        return this.expressionLambda;
    }

    public java.lang.expression.Expression<java.lang.Object, java.lang.Object> getExpression() {
        return this.expression;
    }

    public java.lang.expression.Chain<java.lang.Object, java.lang.Object> getChain() {
        return this.chain;
    }

    public java.lang.expression.Tracing<java.lang.Object, java.lang.Object> getTracing() {
        return this.tracing;
    }
}
