/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.utils.MyPackage;
import ru.yakimov.utils.PackageController;

public class InProtocolHandler extends ChannelInboundHandlerAdapter {


    public static final int DATA_MAX_SIZE = 1024 * 1024 * 1;

    public static final String UNITS_SEPARATOR = "//%//";
    public static final String DATA_DELIMITER = " ";
    public static final String ROOT_DIR = "root/";

    private ByteBuf accumulator;

    private PackageController packageController = new PackageController();


    private int state = -1;
    private int reqLen = -1;
    private ProtocolDataType type = ProtocolDataType.EMPTY;

    private MyPackage myPackage;

    public InProtocolHandler() {
        System.out.println("InProtocolHandler created");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        System.out.println("-------------------------------------------------------");

        if(accumulator == null)
            accumulator = ctx.alloc().directBuffer(DATA_MAX_SIZE);

        ByteBuf buf = ((ByteBuf) msg);


        if (state == -1) {
            myPackage = packageController.getActiveElement();

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            byte firstByte = buf.readByte();
            type = ProtocolDataType.getDataTypeFromByte(firstByte);

            if(type.equals(ProtocolDataType.FILE)) {
                System.err.println("FILE PROTOCOL");
            }



            if(type.equals(ProtocolDataType.EMPTY)) {
                System.err.println("Stage -1 error PACK is EMPTY");
                myPackage.disable();
                buf.release();
                 return;
            }
            myPackage.setType(type);


            state = 0;
            reqLen = 4;
            System.out.println("Получен буфер " + ((type.equals(ProtocolDataType.COMMAND))?"command":"file"));

        }

        if(state == 0){

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            if (buf.readableBytes() < reqLen) {
                System.err.println("Stage 0 error "+buf.readableBytes()+" < "+reqLen);
                return;
            }
            reqLen = buf.readInt();
            myPackage.setCommandLength(reqLen);
            state = 1;

            System.out.println(reqLen);

        }

        if (state == 1) {

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            if (buf.readableBytes() < reqLen) {
                System.err.println("Stage 1 error "+buf.readableBytes()+" < "+reqLen);
                return;
            }
            byte[] commandArr = new byte[reqLen];
            buf.readBytes(commandArr);

            myPackage.setCommandArr(commandArr);
            state = 2;
            reqLen = 4;
            System.out.println(new String(myPackage.getCommandArr()));

        }

        if (state == 2) {

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            if (buf.readableBytes() < reqLen) {
                System.err.println("Stage 2 error "+buf.readableBytes()+" < "+reqLen);

                return;
            }
            reqLen = buf.readInt();
            myPackage.setDataLength(reqLen);
//            outArr[IndexProtocol.DATA_LENGTH.getInt()] = reqLen;
            state = 3;

            System.out.println(reqLen);

        }

        if (state == 3) {

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            accumulator.writeBytes(buf);

            System.out.println("Accumulator " + accumulator.readableBytes());
            if(accumulator.readableBytes() < reqLen){
                buf.release();
                return;
            }

            System.out.println("Прочитанные акамулятором  символы " + accumulator.readableBytes());

            accumulator.readBytes(myPackage.getDataArrForWrite(accumulator.readableBytes()));

            ctx.fireChannelRead(myPackage);

            accumulator.clear();
            state = -1;

        }
        buf.clear();
        buf.release();
        packageController.checkPool();

        System.out.println("-------------------------------------------------------");


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public PackageController getPackageController() {
        return packageController;
    }
}
