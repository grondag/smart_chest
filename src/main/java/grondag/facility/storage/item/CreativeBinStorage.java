/*******************************************************************************
 * Copyright 2019, 2020 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.facility.storage.item;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.FixedArticleFunction;
import grondag.fluidity.base.article.StoredDiscreteArticle;
import grondag.fluidity.base.storage.discrete.AbstractDiscreteStore;
import grondag.fluidity.base.storage.discrete.DividedDiscreteStore;
import grondag.fluidity.base.storage.discrete.FixedDiscreteStore;
import grondag.fluidity.base.storage.helper.FixedArticleManager;

@API(status = Status.EXPERIMENTAL)
public class CreativeBinStorage extends AbstractDiscreteStore<CreativeBinStorage> implements FixedDiscreteStore {
	protected final int divisionCount;
	protected final long capacityPerDivision;

	public CreativeBinStorage(int divisionCount, long capacityPerDivision) {
		super(divisionCount, divisionCount * capacityPerDivision, new FixedArticleManager<>(divisionCount, StoredDiscreteArticle::new));
		this.divisionCount = divisionCount;
		this.capacityPerDivision = capacityPerDivision;
	}

	@Override
	public FixedArticleFunction getConsumer() {
		return consumer;
	}

	@Override
	public boolean hasConsumer() {
		return true;
	}

	@Override
	public FixedArticleFunction getSupplier() {
		return supplier;
	}

	@Override
	public boolean hasSupplier() {
		return true;
	}

	@Override
	protected FixedDiscreteArticleFunction createConsumer() {
		return new Consumer();
	}

	protected class Consumer extends AbstractDiscreteStore<DividedDiscreteStore>.Consumer {
		@Override
		public long apply(Article item, long count, boolean simulate) {
			final StoredDiscreteArticle a = articles.get(item);
			return a == null || a.isEmpty() ? 0 : count;
		}

		@Override
		public long apply(int handle, Article item, long count, boolean simulate) {
			Preconditions.checkArgument(count >= 0, "Request to accept negative items. (%s)", count);
			Preconditions.checkNotNull(item, "Request to accept null item");

			if (item.isNothing() || count == 0 || !filter.test(item)) {
				return 0;
			}

			final StoredDiscreteArticle a = articles.get(handle);

			if(a.isEmpty()) {
				if(!simulate) {
					a.setArticle(item);
					a.setCount(capacityPerDivision);
					notifier.notifyAccept(a, capacityPerDivision);
					dirtyNotifier.run();
				}

				return count;

			} else if (a.article().equals(item)) {
				return count;
			} else {
				return 0;
			}
		}
	}

	@Override
	protected FixedDiscreteArticleFunction createSupplier() {
		return new Supplier();
	}

	protected class Supplier extends AbstractDiscreteStore<DividedDiscreteStore>.Supplier {
		@Override
		public long apply(Article item, long count, boolean simulate) {
			Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
			Preconditions.checkNotNull(item, "Request to supply null item");

			if (item.isNothing() || isEmpty()) {
				return 0;
			}

			final StoredDiscreteArticle article = articles.get(item);

			return(article == null || article.isEmpty()) ? 0 : count;
		}

		@Override
		public long apply(int handle, Article item, long count, boolean simulate) {
			Preconditions.checkArgument(count >= 0, "Request to supply negative items. (%s)", count);
			Preconditions.checkNotNull(item, "Request to supply null item");

			if (item.isNothing() || isEmpty()) {
				return 0;
			}

			final StoredDiscreteArticle a = articles.get(handle);

			return a == null || a.isEmpty() || !a.article().equals(item) ? 0 : count;
		}
	}

	@Override
	public CompoundTag writeTag() {
		final CompoundTag result = new CompoundTag();

		if(!isEmpty()) {
			final ListTag list = new ListTag();
			final int limit = articles.handleCount();

			for (int i = 0; i < limit; i++) {
				final StoredDiscreteArticle a = articles.get(i);

				if(!a.isEmpty()) {
					final CompoundTag aTag = a.toTag();
					aTag.putInt("handle", i);
					list.add(aTag);
				}
			}

			result.put(AbstractDiscreteStore.TAG_ITEMS, list);
		}

		return result;
	}

	@Override
	public void readTag(CompoundTag tag) {
		clear();

		if(tag.contains(AbstractDiscreteStore.TAG_ITEMS)) {
			final ListTag list = tag.getList(AbstractDiscreteStore.TAG_ITEMS, 10);
			final int limit = list.size();
			final StoredDiscreteArticle lookup = new StoredDiscreteArticle();

			for(int i = 0; i < limit; i++) {
				final CompoundTag aTag = list.getCompound(i);
				lookup.readTag(aTag);

				if(!lookup.isEmpty()) {
					consumer.apply(aTag.getInt("handle"), lookup.article(), lookup.count(), false);
				}
			}
		}
	}
}
