package me.darksidecode.kantanj.networking;

public class GetHttpRequest extends SimpleHttpRequest {

    @Override
    public GetHttpRequest done() {
        return this;
    }

    @Override
    public SimpleHttpRequest requestMethod(RequestMethod requestMethod) {
        throw new IllegalStateException("cannot change " +
                "request method for " + getClass().getName());
    }

    @Override
    public RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }

}
