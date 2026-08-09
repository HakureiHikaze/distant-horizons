package com.seibel.distanthorizons.common.render.renderpearl;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.buffers.TransientMemory;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.device.GpuSurface;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.blaze3d.platform.NativeImage;
import org.joml.Vector4fc;
import org.lwjgl.PointerBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * Counting fake {@link GpuDevice} for JVM unit tests. Every method needed by
 * the renderpearl wrappers is implemented; unused paths throw.
 */
public class TestGpuDevice implements GpuDevice
{
	public int createBufferCount = 0;
	public int createTextureCount = 0;
	public int createTextureViewCount = 0;
	public int createSamplerCount = 0;
	public int createCommandEncoderCount = 0;
	public int compilePipelineCount = 0;
	public int writeToBufferCount = 0;
	public int createRenderPassCount = 0;
	public int drawCount = 0;
	public int drawIndexedCount = 0;
	public int setIndexBufferCount = 0;
	public boolean closed = false;
	
	public final List<GpuBuffer> createdBuffers = new ArrayList<>();
	public final List<GpuTexture> createdTextures = new ArrayList<>();
	
	
	
	@Override public GpuSurface createSurface(long windowHandle) { throw new UnsupportedOperationException(); }
	
	@Override public CommandEncoder createCommandEncoder()
	{
		this.createCommandEncoderCount++;
		return new TestCommandEncoder(this);
	}
	
	@Override public GpuTexture createTexture(Supplier<String> label, int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels)
	{
		this.createTextureCount++;
		TestGpuTexture texture = new TestGpuTexture(format, usage, width, height, depthOrLayers, mipLevels, label != null ? label.get() : null);
		this.createdTextures.add(texture);
		return texture;
	}
	
	@Override public GpuTexture createTexture(String label, int usage, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels)
	{
		this.createTextureCount++;
		TestGpuTexture texture = new TestGpuTexture(format, usage, width, height, depthOrLayers, mipLevels, label);
		this.createdTextures.add(texture);
		return texture;
	}
	
	@Override public GpuTextureView createTextureView(GpuTexture texture)
	{
		this.createTextureViewCount++;
		return new TestGpuTextureView(texture);
	}
	
	@Override public GpuTextureView createTextureView(GpuTexture texture, int baseMipLevel, int mipLevels)
	{
		this.createTextureViewCount++;
		return new TestGpuTextureView(texture);
	}
	
	@Override public GpuSampler createSampler(AddressMode addressModeU, AddressMode addressModeV, FilterMode minFilter, FilterMode magFilter, int maxAnisotropy, OptionalDouble maxLod)
	{
		this.createSamplerCount++;
		return new TestGpuSampler();
	}
	
	@Override public GpuBuffer createBuffer(Supplier<String> label, int usage, long size)
	{
		this.createBufferCount++;
		TestGpuBuffer buffer = new TestGpuBuffer(size, usage);
		this.createdBuffers.add(buffer);
		return buffer;
	}
	
	@Override public GpuBuffer createBuffer(Supplier<String> label, int usage, ByteBuffer data)
	{
		this.createBufferCount++;
		TestGpuBuffer buffer = new TestGpuBuffer(data.remaining(), usage);
		this.createdBuffers.add(buffer);
		return buffer;
	}
	
	@Override public List<String> getLastDebugMessages() { return List.of(); }
	
	@Override public boolean isDebuggingEnabled() { return false; }
	
	@Override public void close() { this.closed = true; }
	
	@Override public GpuQueryPool createTimestampQueryPool(int size) { throw new UnsupportedOperationException(); }
	
	@Override public DeviceInfo getDeviceInfo() { throw new UnsupportedOperationException(); }
	
	@Override public CompiledRenderPipeline compilePipeline(RenderPipeline pipeline, ShaderSource shaderSource)
	{
		this.compilePipelineCount++;
		return new TestCompiledRenderPipeline();
	}
	
	
	
	public static class TestCommandEncoder implements CommandEncoder
	{
		private final TestGpuDevice device;
		
		public TestCommandEncoder(TestGpuDevice device) { this.device = device; }
		
		@Override public void submit() { }
		@Override public TransientMemory transientMemory() { throw new UnsupportedOperationException(); }
		@Override public RenderPass createRenderPass(RenderPassDescriptor descriptor)
		{
			this.device.createRenderPassCount++;
			return new TestRenderPass(this.device);
		}
		@Override public void clearColorTexture(GpuTexture colorTexture, Vector4fc clearColor) { }
		@Override public void clearColorAndDepthTextures(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth) { }
		@Override public void clearColorAndDepthTextures(GpuTexture colorTexture, Vector4fc clearColor, GpuTexture depthTexture, double clearDepth, int regionX, int regionY, int regionWidth, int regionHeight, int mipLevel) { }
		@Override public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) { }
		@Override public void writeToBuffer(GpuBufferSlice destination, ByteBuffer data) { this.device.writeToBufferCount++; }
		@Override public void copyToBuffer(GpuBufferSlice source, GpuBufferSlice target) { }
		@Override public void writeToTexture(GpuTexture destination, NativeImage source) { }
		@Override public void writeToTexture(GpuTexture destination, NativeImage source, int mipLevel, int depthOrLayer, int destX, int destY) { }
		@Override public void writeToTexture(GpuTexture destination, ByteBuffer source, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height) { }
		@Override public void copyBufferToTexture(GpuBufferSlice source, int sourceX, int sourceY, int sourceWidth, int sourceHeight, GpuTexture destination, int destinationX, int destinationY, int copyWidth, int copyHeight, int mipLevel, int arrayLayer) { }
		@Override public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel) { }
		@Override public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel, int x, int y, int width, int height) { }
		@Override public void copyTextureToTexture(GpuTexture source, GpuTexture destination, int mipLevel, int destX, int destY, int sourceX, int sourceY, int width, int height) { }
		@Override public GpuFence createFence() { throw new UnsupportedOperationException(); }
		@Override public void writeTimestamp(GpuQueryPool pool, int index) { }
	}
	
	public static class TestRenderPass implements RenderPass
	{
		private final TestGpuDevice device;
		private boolean closed = false;
		
		public TestRenderPass(TestGpuDevice device) { this.device = device; }
		
		@Override public void pushDebugGroup(Supplier<String> label) { }
		@Override public void popDebugGroup() { }
		@Override public void writeTimestamp(GpuQueryPool pool, int index) { }
		@Override public void setPipeline(CompiledRenderPipeline pipeline) { }
		@Override public void setUniform(String name, GpuTextureView textureView, GpuSampler sampler) { }
		@Override public void setUniform(String name, GpuBuffer value) { }
		@Override public void setUniform(String name, GpuBufferSlice value) { }
		@Override public void pushConstants(ByteBuffer value) { }
		@Override public void enableScissor(int x, int y, int width, int height) { }
		@Override public void disableScissor() { }
		@Override public void setVertexBuffer(int slot, GpuBufferSlice vertexBuffer) { }
		@Override public void setIndexBuffer(GpuBuffer indexBuffer, IndexType indexType) { this.device.setIndexBufferCount++; }
		@Override public void drawIndexed(int indexCount, int instanceCount, int firstIndex, int vertexOffset, int firstInstance) { this.device.drawIndexedCount++; }
		@Override public void multiDrawIndexed(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) { }
		@Override public void multiDrawIndexed(PointerBuffer firstIndexOffsets, IntBuffer indexCounts, IntBuffer vertexOffsets, int drawCount) { }
		@Override public void drawIndexedIndirect(GpuBufferSlice commands, int drawCount) { }
		@Override public <T> void drawMultipleIndexed(Collection<RenderPass.Draw<T>> draws, GpuBuffer defaultIndexBuffer, IndexType defaultIndexType, Collection<String> dynamicUniforms, T uniformArgument) { }
		@Override public void draw(int vertexCount, int instanceCount, int firstVertex, int firstInstance) { this.device.drawCount++; }
		@Override public void multiDraw(IntBuffer drawParameters, int instanceCount, int firstInstance, int drawCount) { }
		@Override public void multiDraw(IntBuffer firstVertices, IntBuffer vertexCounts, int drawCount) { }
		@Override public void drawIndirect(GpuBufferSlice commands, int drawCount) { }
		@Override public void close() { this.closed = true; }
		
		public boolean isClosed() { return this.closed; }
	}
	
	public static class TestGpuBuffer implements GpuBuffer
	{
		private final long size;
		private final int usage;
		private boolean closed = false;
		private final ByteBuffer mappedData = ByteBuffer.allocateDirect(512);
		
		public TestGpuBuffer(long size, int usage) { this.size = size; this.usage = usage; }
		
		@Override public long size() { return this.size; }
		@Override public int usage() { return this.usage; }
		@Override public boolean isClosed() { return this.closed; }
		@Override public GpuBufferSlice.MappedView map(long offset, long length, boolean read, boolean write)
		{
			ByteBuffer view = this.mappedData.duplicate();
			view.position((int) offset);
			view.limit((int) (offset + length));
			return new GpuBufferSlice.MappedView(new GpuBufferSlice(this, offset, length), view, () -> { });
		}
		@Override public void close() { this.closed = true; }
	}
	
	public static class TestGpuTexture implements GpuTexture
	{
		private final GpuFormat format;
		private final int usage;
		private final int width;
		private final int height;
		private final int depthOrLayers;
		private final int mipLevels;
		private final String label;
		private boolean closed = false;
		
		public TestGpuTexture(GpuFormat format, int usage, int width, int height, int depthOrLayers, int mipLevels, String label)
		{
			this.format = format;
			this.usage = usage;
			this.width = width;
			this.height = height;
			this.depthOrLayers = depthOrLayers;
			this.mipLevels = mipLevels;
			this.label = label;
		}
		
		@Override public int getWidth(int mipLevel) { return this.width; }
		@Override public int getHeight(int mipLevel) { return this.height; }
		@Override public int getDepthOrLayers() { return this.depthOrLayers; }
		@Override public int getMipLevels() { return this.mipLevels; }
		@Override public GpuFormat getFormat() { return this.format; }
		@Override public int usage() { return this.usage; }
		@Override public String getLabel() { return this.label; }
		@Override public boolean isClosed() { return this.closed; }
		@Override public void close() { this.closed = true; }
	}
	
	public static class TestGpuTextureView implements GpuTextureView
	{
		private final GpuTexture texture;
		private boolean closed = false;
		
		public TestGpuTextureView(GpuTexture texture) { this.texture = texture; }
		
		@Override public boolean isClosed() { return this.closed; }
		@Override public GpuTexture texture() { return this.texture; }
		@Override public int baseMipLevel() { return 0; }
		@Override public int mipLevels() { return 1; }
		@Override public int getWidth(int mipLevel) { return this.texture.getWidth(mipLevel); }
		@Override public int getHeight(int mipLevel) { return this.texture.getHeight(mipLevel); }
		@Override public void close() { this.closed = true; }
	}
	
	public static class TestGpuSampler implements GpuSampler
	{
		private boolean closed = false;
		
		@Override public AddressMode getAddressModeU() { return AddressMode.CLAMP_TO_EDGE; }
		@Override public AddressMode getAddressModeV() { return AddressMode.CLAMP_TO_EDGE; }
		@Override public FilterMode getMinFilter() { return FilterMode.LINEAR; }
		@Override public FilterMode getMagFilter() { return FilterMode.LINEAR; }
		@Override public int getMaxAnisotropy() { return 1; }
		@Override public OptionalDouble getMaxLod() { return OptionalDouble.empty(); }
		@Override public boolean isClosed() { return this.closed; }
		@Override public void close() { this.closed = true; }
	}
	
	public static class TestCompiledRenderPipeline implements CompiledRenderPipeline
	{
		private boolean closed = false;
		
		@Override public boolean isClosed() { return this.closed; }
		@Override public void close() { this.closed = true; }
	}
	
}
