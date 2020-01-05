package grondag.facility.transport;

import java.util.function.Function;

import io.netty.util.internal.ThreadLocalRandom;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.device.DeviceComponent;
import grondag.fluidity.api.device.DeviceComponentType;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.wip.base.transport.BasicCarrier;
import grondag.fluidity.wip.base.transport.BasicCarrierSession;
import grondag.fluidity.wip.base.transport.BroadcastConsumer;
import grondag.fluidity.wip.base.transport.BroadcastSupplier;

public class UtbCarrierSession extends BasicCarrierSession<UtbCostFunction> {
	public UtbCarrierSession(BasicCarrier<UtbCostFunction> carrier, Function<DeviceComponentType<?>, DeviceComponent<?>> componentFunction) {
		super(carrier, componentFunction);
	}

	@Override
	protected UtbBroadcastConsumer createBroadcastConsumer() {
		return new UtbBroadcastConsumer(this);
	}

	@Override
	protected UtbBroadcastSupplier createBroadcastSupplier() {
		return new UtbBroadcastSupplier(this);
	}

	boolean shouldTransmit() {
		final int tickRange = carrier.effectiveCarrier().costFunction().backoffTickRange();
		return tickRange == 1 ? true : ThreadLocalRandom.current().nextInt(tickRange) == 0;
	}

	protected class UtbBroadcastSupplier extends BroadcastSupplier<UtbCostFunction> {
		public UtbBroadcastSupplier(UtbCarrierSession fromNode) {
			super(fromNode);
		}

		@Override
		public long apply(Article item, long count, boolean simulate) {
			return shouldTransmit() ? super.apply(item, count, simulate) : 0;
		}

		@Override
		public FractionView apply(Article item, FractionView volume, boolean simulate) {
			return shouldTransmit() ? super.apply(item, volume, simulate) : Fraction.ZERO;
		}

		@Override
		public long apply(Article item, long numerator, long divisor, boolean simulate) {
			return shouldTransmit() ? super.apply(item, numerator, divisor, simulate) : 0;
		}
	}

	protected class UtbBroadcastConsumer extends BroadcastConsumer<UtbCostFunction> {
		public UtbBroadcastConsumer(UtbCarrierSession fromNode) {
			super(fromNode);
		}

		@Override
		public long apply(Article item, long count, boolean simulate) {
			return shouldTransmit() ? super.apply(item, count, simulate) : 0;
		}

		@Override
		public FractionView apply(Article item, FractionView volume, boolean simulate) {
			return shouldTransmit() ? super.apply(item, volume, simulate) : Fraction.ZERO;
		}

		@Override
		public long apply(Article item, long numerator, long divisor, boolean simulate) {
			return shouldTransmit() ? super.apply(item, numerator, divisor, simulate) : 0;
		}
	}
}