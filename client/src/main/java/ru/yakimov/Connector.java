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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import ru.yakimov.handlers.InProtocolHandler;
import ru.yakimov.handlers.OutProtocolHandler;
import ru.yakimov.handlers.ProtocolDataType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class Connector {

    private static int windowWeight = 618;
    private static int windowHeight = 640;

    private static Connector instance;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private Controller controller;
    private VerificationController verificationController;

    private Scene sampleScene;
    private Scene verificationScene;

    private BooleanProperty connected = new SimpleBooleanProperty(false);
    private Channel channel;
    private EventLoopGroup group;

    private Object[] dataForSend = new Object[5];

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

    private Connector() {

        try {

            FXMLLoader sampleLoader = new FXMLLoader();
            Parent sampleRoot = sampleLoader.load(getClass().getResourceAsStream("/sample.fxml"));
            sampleScene = new Scene(sampleRoot, windowWeight, windowHeight);
            controller = sampleLoader.getController();

            FXMLLoader loginLoader = new FXMLLoader();
            Parent loginRoot = loginLoader.load(getClass().getResourceAsStream("/login.fxml"));
            verificationScene = new Scene(loginRoot, windowWeight, windowHeight);
            verificationController = loginLoader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void connect() {

        if( connected.get() ) {
            System.out.println("Connect is already exist");
            return;
        }

        String host = "localhost";
        int port = 8189;

        group = new NioEventLoopGroup();

        Task<Channel> task = new Task<Channel>() {

            @Override
            protected Channel call() throws Exception {

                updateMessage("Bootstrapping");
                updateProgress(0.1d, 1.0d);

                Bootstrap b = new Bootstrap();
                b
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .remoteAddress( new InetSocketAddress(host, port) )
                        .handler( new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new OutProtocolHandler(),new InProtocolHandler(), new VerificationHandler(), new CloudHandler());
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

                countDownLatch.countDown();

            }

            @Override
            protected void failed() {

                Throwable exc = getException();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Client");
                alert.setHeaderText( exc.getClass().getName() );
                alert.setContentText( exc.getMessage() );
                alert.showAndWait();

                connected.set(false);

                exc.printStackTrace();

            }
        };

        new Thread(task).start();
    }

    @FXML
    public void send() {
        if(!connected.get()) {
            connect();

        }
        try {
            countDownLatch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        final String toSend = "";

        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {


                ChannelFuture f = channel.writeAndFlush(dataForSend);
                f.sync();


                return null;
            }

            @Override
            protected void failed() {

                Throwable exc = getException();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Client");
                alert.setHeaderText( exc.getClass().getName() );
                alert.setContentText( exc.getMessage() );
                alert.showAndWait();

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

                Throwable t = getException();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Client");
                alert.setHeaderText( t.getClass().getName() );
                alert.setContentText( t.getMessage() );
                alert.showAndWait();

                t.printStackTrace();

            }

        };



        new Thread(task).start();
    }

    public void setCommandProtocol(String command, String strData){
        setProtocol(ProtocolDataType.COMMAND, command.getBytes(), strData.getBytes());
    }

    public void setProtocol(ProtocolDataType type, byte[] commandArr, byte[] data){
        dataForSend[0] = type;
        dataForSend[1] = commandArr.length;
        dataForSend[2] = commandArr;
        dataForSend[3] = data.length;
        dataForSend[4] = data;

    }

    public Controller getController() {
        return controller;
    }

    public VerificationController getVerificationController() {
        return verificationController;
    }

    public Scene getSampleScene() {
        return sampleScene;
    }

    public Scene getVerificationScene() {
        return verificationScene;
    }

    public void setVerificationController(VerificationController verificationController) {
        this.verificationController = verificationController;
    }

    public void setVerificationScene(Scene verificationScene) {
        this.verificationScene = verificationScene;
    }
}
