/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import ru.yakimov.handlers.InProtocolHandler;
import ru.yakimov.handlers.OutProtocolHandler;
import ru.yakimov.utils.MyPackage;
import ru.yakimov.utils.PackageController;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Connector {


    private static Connector instance;


    private BooleanProperty connected ;
    private Channel channel;
    private EventLoopGroup group;

    private PackageController packageController;
    private BlockingQueue<MyPackage> queue;




    public Connector() {
        this.connected = new SimpleBooleanProperty(false);
        this.queue = new ArrayBlockingQueue<>(80);

    }

    public static Connector getInstance(){
        Connector localInstance = instance;
        if(localInstance == null){
            synchronized (Connector.class){
                localInstance = instance;
                if(localInstance == null){
                    localInstance = instance = new Connector();
                }
            }
        }
        return  localInstance;
    }

    public void addToQueue(MyPackage myPackage){
        queue.add(myPackage);
    }


    public void connect() {

        if( connected.get() ) {
            System.out.println("Connect is already exist");
            return;
        }

        System.out.println("Connection");

        String host = "localhost";
        int port = 8189;

        group = new NioEventLoopGroup();

        Task<Channel> task = new Task<Channel>() {

            @Override
            protected Channel call() throws Exception {

                updateMessage("Bootstrapping");
                updateProgress(0.1d, 1.0d);

                InProtocolHandler inProtocolHandler = new InProtocolHandler();
                packageController = inProtocolHandler.getPackageController();

                Bootstrap b = new Bootstrap();
                b
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress( new InetSocketAddress(host, port) )
                        .handler( new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new OutProtocolHandler(), inProtocolHandler
                                        , new VerificationHandler(), new CloudHandler(), new FileDownloadHandler());
                            }
                        });

                System.out.println("point");
                ChannelFuture f = b.connect();

                Channel chn = f.channel();

                updateMessage("Connecting");
                updateProgress(0.2d, 1.0d);

                f.sync();

                return chn;
            }

            @Override
            protected void succeeded() {

                channel = getValue();
                connected.set(true);
                sending();

            }

            @Override
            protected void failed() {

                Throwable exc = getException();
                SceneAssets.getInstance().getController().showAlertError(exc.getClass().getName() ,  exc.getMessage());
                connected.set(false);

                exc.printStackTrace();

            }
        };


        new Thread(task).start();
    }

    public void sending() {

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                ChannelFuture f = null;
                while(connected.get()){
                    MyPackage myPackage = queue.take();
                    f = channel.writeAndFlush(myPackage);

                    if(!channel.isOpen())
                        break;
                }
                f.sync();
                return null;
            }

            @Override
            protected void failed() {
                Throwable exc = getException();
                SceneAssets.getInstance().getController().showAlertError(exc.getClass().getName() ,  exc.getMessage());
                connected.set(false);

                exc.printStackTrace();
            }
        };


        new Thread(task).start();
    }



    @FXML
    public void disconnect() {

        if( !connected.get() ) {
            System.out.println("There is not connection");
            return;
        }

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                updateMessage("Disconnecting");
                updateProgress(0.1d, 1.0d);

                channel.close().sync();

                updateMessage("Closing group");
                updateProgress(0.5d, 1.0d);
                group.shutdownGracefully().sync();

                return null;
            }

            @Override
            protected void succeeded() {

                connected.set(false);
            }

            @Override
            protected void failed() {
                connected.set(false);
                Throwable exc = getException();
                SceneAssets.getInstance().getController().showAlertError(exc.getClass().getName() ,  exc.getMessage());
                exc.printStackTrace();
            }

        };

        new Thread(task).start();
    }





    public void addToSend(ProtocolDataType type, Commands command, byte[] dataArr){
        if(!connected.get())
            connect();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MyPackage myPackage = packageController.getActiveElement();
        myPackage.set(type, command.getString().getBytes(), dataArr);
        queue.add(myPackage);
    }

    public void setAndSendFile(Commands command, byte[] dataArr){
        addToSend(ProtocolDataType.FILE, command, dataArr);
    }

    public void setAndSendFile(Commands command, int length, byte[] dataArr){
        addToSend(ProtocolDataType.FILE, command, dataArr);
    }

    public void setAndSendCommand(Commands command, byte[] dataArr){
        System.out.println("Sending command");
        addToSend(ProtocolDataType.COMMAND, command, dataArr);

    }

    public void addPackageToQueue(MyPackage myPackage){
        queue.add(myPackage);

    }

    public MyPackage getPackage() {
        return packageController.getActiveElement();
    }
}
