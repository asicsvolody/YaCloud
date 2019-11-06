/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;


public class OutProtocolHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Получено задание на отправку");
        Object[] dataArr = (Object[]) msg;


        ProtocolDataType dataType = ((ProtocolDataType) dataArr[0]);

        ByteBufAllocator al = new PooledByteBufAllocator();
        ByteBuf byteBuffer = al.buffer(4);
        byteBuffer.writeByte(((ProtocolDataType) dataArr[0]).getFirstMessageByte());
        ctx.writeAndFlush(byteBuffer);

        if(dataType.equals(ProtocolDataType.EMPTY))
            return;

        byteBuffer = al.buffer(4);
        byteBuffer.writeInt(((int) dataArr[1]));
        ctx.writeAndFlush(byteBuffer);


        byteBuffer = al.buffer((int) dataArr[1]);
        byteBuffer.writeBytes(((byte[]) dataArr[2]));
        ctx.writeAndFlush(byteBuffer);

        byteBuffer = al.buffer(4);
        byteBuffer.writeInt(((int) dataArr[3]));
        ctx.writeAndFlush(byteBuffer);

        byteBuffer = al.buffer((int) dataArr[3]);
        byteBuffer.writeBytes(((byte[]) dataArr[4]));
        ctx.writeAndFlush(byteBuffer);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
