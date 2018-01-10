package rpc.service.Iface;

import java.util.List;

import rpc.core.model.EndPoint;
import rpc.core.model.Result;

public interface Iface {

    public List<Result> connectAndExcute(EndPoint endPoint, List<String> cmd) throws org.apache.thrift.TException;

    public Result connect(EndPoint endPoint) throws org.apache.thrift.TException;

    public Result excuteCMD(String cmd, String sessionId) throws org.apache.thrift.TException;

    public Result close( String sessionId) throws org.apache.thrift.TException;

  }