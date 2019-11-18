/**
 * Created by IntelliJ Idea.
 * User: Якимов В.Н.
 * E-mail: yakimovvn@bk.ru
 */

package ru.yakimov.handlers;

import com.sun.tools.javac.code.Attribute;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import ru.yakimov.ProtocolDataType;
import ru.yakimov.utils.MyPackage;


public class OutProtocolHandler extends ChannelOutboundHandlerAdapter {

    private ByteBuf accumulator;


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.err.println("Получено задание на отправку");

        MyPackage myPackage = ((MyPackage) msg);

        if(new String(myPackage.getCommandArr()).equals("startFile") || new String(myPackage.getCommandArr()).equals("file")){
            System.out.println();
        }

        if(accumulator == null)
            accumulator = ctx.alloc().directBuffer(InProtocolHandler.DATA_MAX_SIZE + 2048);

        ProtocolDataType dataType = myPackage.getType();

        if(dataType.equals(ProtocolDataType.EMPTY)) {
            System.err.println("Protocol is empty");
            accumulator.writeByte(dataType.getFirstMessageByte());
            ctx.writeAndFlush(accumulator);
            accumulator.clear();
            myPackage.disable();

            return;
        }


        accumulator.writeByte(myPackage.getType().getFirstMessageByte());

        accumulator.writeInt(myPackage.getCommandLength());

        accumulator.writeBytes(myPackage.getCommandArr());

        accumulator.writeInt(myPackage.getDataLength());

        accumulator.writeBytes(myPackage.getDataArrForRead());

        System.out.println("Writable bites before "+ accumulator.readableBytes());

        ctx.writeAndFlush(accumulator.retain());

        myPackage.disable();

        accumulator.clear();


        System.out.println("Writable bites after "+ accumulator.readableBytes());

        ctx.pipeline().get(InProtocolHandler.class).getPackageController().checkPool();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
