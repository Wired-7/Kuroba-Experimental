/*
 * KurobaEx - *chan browser https://github.com/K1rakishou/Kuroba-Experimental/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.core.site;

import androidx.collection.ArrayMap;

import com.github.adamantcheese.chan.core.model.Post;
import com.github.adamantcheese.model.data.board.ChanBoard;
import com.github.adamantcheese.model.data.descriptor.BoardDescriptor;
import com.github.adamantcheese.model.data.descriptor.ChanDescriptor;

import java.util.Map;

import okhttp3.HttpUrl;

/**
 * Endpoints for {@link Site}.
 */
public interface SiteEndpoints {

    HttpUrl catalog(BoardDescriptor boardDescriptor);

    HttpUrl thread(ChanDescriptor.ThreadDescriptor threadDescriptor);

    HttpUrl imageUrl(Post.Builder post, Map<String, String> arg);

    HttpUrl thumbnailUrl(Post.Builder post, boolean spoiler, int customSpoilers, Map<String, String> arg);

    HttpUrl icon(String icon, Map<String, String> arg);

    HttpUrl boards();

    HttpUrl pages(ChanBoard board);

    HttpUrl archive(ChanBoard board);

    HttpUrl reply(ChanDescriptor chanDescriptor);

    HttpUrl delete(Post post);

    HttpUrl report(Post post);

    HttpUrl login();

    static Map<String, String> makeArgument(String key, String value) {
        Map<String, String> map = new ArrayMap<>(1);
        map.put(key, value);
        return map;
    }

    static Map<String, String> makeArgument(String key1, String value1, String key2, String value2) {
        Map<String, String> map = new ArrayMap<>(2);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
