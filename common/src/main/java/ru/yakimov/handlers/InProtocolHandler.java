/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InProtocolHandler extends ChannelInboundHandlerAdapter {


    private int state = -1;
    private int reqLen = -1;
    private ProtocolDataType type = ProtocolDataType.EMPTY;

    private Object[] outArr = new Object[5];

    public InProtocolHandler() {
        System.out.println("InProtocolHandler created");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("Получен буфер");
        ByteBuf buf = ((ByteBuf) msg);


        if (state == -1) {
            byte firstByte = buf.readByte();
            type = ProtocolDataType.getDataTypeFromByte(firstByte);

            if(type.equals(ProtocolDataType.EMPTY))
                return;
            outArr[0]= type;
            state = 0;
            reqLen = 4;

            System.out.println(type.getFirstMessageByte());
        }
        if(state == 0){
            if (buf.readableBytes() < reqLen) {
                return;
            }
            reqLen = buf.readInt();
            outArr[1] = reqLen;
            state = 1;

            System.out.println(reqLen);

        }

        if (state == 1) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] info = new byte[reqLen];
            buf.readBytes(info);
            outArr[2] = info;
            state = 2;

            System.out.println(new String(info));
        }

        if (state == 2) {
            reqLen = buf.readInt();
            outArr[3] = reqLen;
            state = 3;

            System.out.println(reqLen);

        }

        if (state == 3) {
            if (buf.readableBytes() < reqLen) {
                return;
            }
            byte[] data = new byte[reqLen];
            buf.readBytes(data);
            outArr[4] = data;
            ctx.fireChannelRead(outArr);
            buf.release();
            state = -1;

            System.out.println(new String(data));

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
