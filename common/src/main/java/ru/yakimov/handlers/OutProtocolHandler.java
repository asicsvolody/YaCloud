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
import ru.yakimov.ProtocolDataType;
import ru.yakimov.utils.MyPackage;


public class OutProtocolHandler extends ChannelOutboundHandlerAdapter {


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("Получено задание на отправку");

        MyPackage myPackage = ((MyPackage) msg);

        ProtocolDataType dataType = myPackage.getType();

        ByteBufAllocator al = new PooledByteBufAllocator();

        if(dataType.equals(ProtocolDataType.EMPTY)) {
            ctx.writeAndFlush(al.buffer(1).writeByte(dataType.getFirstMessageByte()));
            myPackage.disable();
            return;
        }

        int dataBufferSize = 1 + 4 + myPackage.getCommandLength() + 4 + myPackage.getDataLength();

        ByteBuf byteBuffer = al.buffer(dataBufferSize);

        byteBuffer.writeByte(myPackage.getType().getFirstMessageByte());

        byteBuffer.writeInt(myPackage.getCommandLength());
        byteBuffer.writeBytes(myPackage.getCommandArr());
        byteBuffer.writeInt(myPackage.getDataLength());
        byteBuffer.writeBytes(myPackage.getDataArrForRead());

        ctx.writeAndFlush(byteBuffer);

        myPackage.disable();
        ctx.pipeline().get(InProtocolHandler.class).getPackageController().checkPool();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
