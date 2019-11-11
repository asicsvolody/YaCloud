/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.yakimov.IndexProtocol;
import ru.yakimov.ProtocolDataType;

public class InProtocolHandler extends ChannelInboundHandlerAdapter {

    public static final String UNITS_SEPARATOR = "//%//";
    public static final String DATA_DELIMITER = " ";
    public static final String ROOT_DIR = "root/";

    ByteBufAllocator allocator ;

    private ByteBuf accumulator;


    private int state = -1;
    private int reqLen = -1;
    private ProtocolDataType type = ProtocolDataType.EMPTY;

    private Object[] outArr = new Object[5];

    public InProtocolHandler() {
        System.out.println("InProtocolHandler created");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = ((ByteBuf) msg);

        if (state == -1) {

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            byte firstByte = buf.readByte();
            type = ProtocolDataType.getDataTypeFromByte(firstByte);

            if(type.equals(ProtocolDataType.EMPTY)) {
                System.err.println("Stage -1 error: "+type.getFirstMessageByte());
                 return;
            }
            outArr[IndexProtocol.TYPE.getInt()]= type;
            state = 0;
            reqLen = 4;
            System.out.println("-------------------------------------------------------");
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
            outArr[IndexProtocol.COMMAND_LENGTH.getInt()] = reqLen;
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
            byte[] info = new byte[reqLen];
            buf.readBytes(info);
            outArr[IndexProtocol.COMMAND.getInt()] = info;
            state = 2;
            reqLen = 4;
            System.out.println(new String(info));
        }

        if (state == 2) {

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            if (buf.readableBytes() < reqLen) {
                System.err.println("Stage 2 error "+buf.readableBytes()+" < "+reqLen);

                return;
            }
            reqLen = buf.readInt();
            outArr[IndexProtocol.DATA_LENGTH.getInt()] = reqLen;
            state = 3;

            System.out.println(reqLen);

        }

        if (state == 3) {

            System.out.println("reader index"+buf.readerIndex());
            System.out.println("writer index"+buf.writerIndex());

            if(allocator == null){
                allocator = ctx.alloc();
            }
            if(accumulator == null){
                accumulator = allocator.directBuffer(reqLen);
            }

            accumulator.writeBytes(buf);

            if(accumulator.readableBytes() < reqLen){
                buf.release();
                return;
            }

            byte[] data = new byte[reqLen];
            accumulator.readBytes(data);
            outArr[IndexProtocol.DATA.getInt()] = data;

            ctx.fireChannelRead(outArr);

            state = -1;

            allocator = null;
            accumulator = null;

        }
        buf.release();


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
